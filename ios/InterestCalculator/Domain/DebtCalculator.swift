//
//  DebtCalculator.swift
//  InterestCalculator
//

import Foundation

enum DebtCalculator {
    
    private final class ItemTracker {
        let item: DebtSourceItem
        let effectiveRate: Double
        let termMonths: Int
        var currentBalance: Double
        
        init(item: DebtSourceItem, effectiveRate: Double, termMonths: Int, currentBalance: Double) {
            self.item = item
            self.effectiveRate = effectiveRate
            self.termMonths = termMonths
            self.currentBalance = currentBalance
        }
    }
    
    static func calculate(
        items: [DebtSourceItem],
        startDateStr: String = DateTimeUtils.todayString(),
        bankRate: Double = 10.5,
        tctdRate: Double = 28.0,
        method: LoanMethod = .reducingBalance
    ) -> DebtScheduleResult {
        let validItems = items.filter { $0.currentDebt > 0 }
        if validItems.isEmpty {
            return DebtScheduleResult(
                totalInitialDebt: 0.0,
                bankInitialDebt: 0.0,
                tctdInitialDebt: 0.0,
                totalInterest: 0.0,
                bankTotalInterest: 0.0,
                tctdTotalInterest: 0.0,
                totalPayment: 0.0,
                firstMonthPayment: 0.0,
                maxMonthPayment: 0.0,
                weightedAverageRate: 0.0,
                totalMonths: 0,
                schedule: []
            )
        }
        
        let trackers: [ItemTracker] = validItems.map { item in
            let rate = item.customRate ?? (item.sourceType == .bank ? bankRate : tctdRate)
            let targetMaturity = !item.maturityDate.isEmpty ? item.maturityDate : DateTimeUtils.addMonths(startDateStr, 12)
            let months = max(1, DateTimeUtils.monthsBetween(startDateStr, targetMaturity))
            return ItemTracker(
                item: item,
                effectiveRate: rate,
                termMonths: months,
                currentBalance: item.currentDebt
            )
        }
        
        let totalInitialDebt = validItems.reduce(0.0) { $0 + $1.currentDebt }
        let bankInitialDebt = validItems.filter { $0.sourceType == .bank }.reduce(0.0) { $0 + $1.currentDebt }
        let tctdInitialDebt = validItems.filter { $0.sourceType == .creditInstitution }.reduce(0.0) { $0 + $1.currentDebt }
        
        let weightedRate: Double
        if totalInitialDebt > 0 {
            weightedRate = trackers.reduce(0.0) { $0 + ($1.item.currentDebt * $1.effectiveRate) } / totalInitialDebt
        } else {
            weightedRate = 0.0
        }
        
        let maxMonths = trackers.map { $0.termMonths }.max() ?? 1
        var schedule: [DebtPeriodBreakdown] = []
        
        var totalInterestAccumulated = 0.0
        var bankInterestAccumulated = 0.0
        var tctdInterestAccumulated = 0.0
        
        for m in 1...maxMonths {
            let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, m)
            
            var periodPrincipal = 0.0
            var periodInterest = 0.0
            var periodBankPrincipal = 0.0
            var periodBankInterest = 0.0
            var periodTctdPrincipal = 0.0
            var periodTctdInterest = 0.0
            
            var itemPayments: [DebtItemPeriodPayment] = []
            
            for tracker in trackers {
                if m > tracker.termMonths || tracker.currentBalance <= 0.0 {
                    continue
                }
                
                let monthlyRate = (tracker.effectiveRate / 100.0) / 12.0
                let remainingPeriods = tracker.termMonths - m + 1
                let isLastMonth = (m == tracker.termMonths)
                
                let interestPayment: Double
                let principalPayment: Double
                
                switch method {
                case .endOfTerm:
                    interestPayment = tracker.currentBalance * monthlyRate
                    principalPayment = isLastMonth ? tracker.currentBalance : 0.0
                    
                case .flatRate:
                    interestPayment = tracker.item.currentDebt * monthlyRate
                    let basePrincipal = tracker.item.currentDebt / Double(tracker.termMonths)
                    principalPayment = isLastMonth ? tracker.currentBalance : min(basePrincipal, tracker.currentBalance)
                    
                case .annuity:
                    let pmt: Double
                    if monthlyRate > 0 {
                        let factor = pow(1.0 + monthlyRate, Double(remainingPeriods))
                        pmt = tracker.currentBalance * (monthlyRate * factor) / (factor - 1.0)
                    } else {
                        pmt = tracker.currentBalance / Double(remainingPeriods)
                    }
                    let iVal = tracker.currentBalance * monthlyRate
                    let pVal = isLastMonth ? tracker.currentBalance : max(0.0, min(pmt - iVal, tracker.currentBalance))
                    interestPayment = iVal
                    principalPayment = pVal
                    
                case .reducingBalance:
                    let iVal = tracker.currentBalance * monthlyRate
                    let basePrincipal = tracker.item.currentDebt / Double(tracker.termMonths)
                    let pVal = isLastMonth ? tracker.currentBalance : min(basePrincipal, tracker.currentBalance)
                    interestPayment = iVal
                    principalPayment = pVal
                }
                
                let newBal = max(0.0, tracker.currentBalance - principalPayment)
                tracker.currentBalance = newBal
                
                periodPrincipal += principalPayment
                periodInterest += interestPayment
                
                if tracker.item.sourceType == .bank {
                    periodBankPrincipal += principalPayment
                    periodBankInterest += interestPayment
                } else {
                    periodTctdPrincipal += principalPayment
                    periodTctdInterest += interestPayment
                }
                
                let itemName = tracker.item.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? tracker.item.sourceType.title : tracker.item.name
                itemPayments.append(
                    DebtItemPeriodPayment(
                        itemId: tracker.item.id,
                        itemName: itemName,
                        sourceType: tracker.item.sourceType,
                        principalPayment: round(principalPayment),
                        interestPayment: round(interestPayment),
                        totalPayment: round(principalPayment + interestPayment),
                        remainingBalance: round(newBal)
                    )
                )
            }
            
            let totalRemaining = trackers.reduce(0.0) { $0 + $1.currentBalance }
            let periodTotal = periodPrincipal + periodInterest
            
            totalInterestAccumulated += periodInterest
            bankInterestAccumulated += periodBankInterest
            tctdInterestAccumulated += periodTctdInterest
            
            schedule.append(
                DebtPeriodBreakdown(
                    period: m,
                    paymentDate: paymentDate,
                    principalPayment: round(periodPrincipal),
                    interestPayment: round(periodInterest),
                    totalPayment: round(periodTotal),
                    remainingBalance: round(totalRemaining),
                    bankPrincipalPayment: round(periodBankPrincipal),
                    bankInterestPayment: round(periodBankInterest),
                    tctdPrincipalPayment: round(periodTctdPrincipal),
                    tctdInterestPayment: round(periodTctdInterest),
                    itemBreakdowns: itemPayments
                )
            )
        }
        
        let firstMonth = schedule.first?.totalPayment ?? 0.0
        let maxMonth = schedule.map { $0.totalPayment }.max() ?? 0.0
        
        return DebtScheduleResult(
            totalInitialDebt: round(totalInitialDebt),
            bankInitialDebt: round(bankInitialDebt),
            tctdInitialDebt: round(tctdInitialDebt),
            totalInterest: round(totalInterestAccumulated),
            bankTotalInterest: round(bankInterestAccumulated),
            tctdTotalInterest: round(tctdInterestAccumulated),
            totalPayment: round(totalInitialDebt + totalInterestAccumulated),
            firstMonthPayment: firstMonth,
            maxMonthPayment: maxMonth,
            weightedAverageRate: round(weightedRate * 100.0) / 100.0,
            totalMonths: maxMonths,
            schedule: schedule
        )
    }
    
    static func generateDebtConsultationText(
        customerName: String,
        startDateStr: String,
        bankRate: Double,
        tctdRate: Double,
        items: [DebtSourceItem],
        result: DebtScheduleResult
    ) -> String {
        let name = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Quý khách" : customerName
        var lines: [String] = []
        lines.append("📑 BẢNG TỔNG HỢP DƯ NỢ & DÒNG TIỀN TRẢ NỢ HÀNG THÁNG")
        lines.append("Kính gửi: Anh/Chị \(name)")
        lines.append("• Mốc thời gian tổng hợp: \(startDateStr)")
        lines.append("--------------------------------------")
        lines.append("📊 DANH SÁCH CÁC KHOẢN DƯ NỢ HIỆN TẠI:")
        
        let bankItems = items.filter { $0.sourceType == .bank && $0.currentDebt > 0 }
        let tctdItems = items.filter { $0.sourceType == .creditInstitution && $0.currentDebt > 0 }
        
        if !bankItems.isEmpty {
            lines.append("1. KHỐI NGÂN HÀNG (Lãi suất áp dụng: \(bankRate)%/năm):")
            for (idx, item) in bankItems.enumerated() {
                let rate = item.customRate ?? bankRate
                let customStr = item.customRate != nil ? " (Lãi riêng: \(rate)%)" : ""
                let matStr = !item.maturityDate.isEmpty ? " - Đáo hạn: \(item.maturityDate)" : ""
                let itemName = item.name.isEmpty ? "Ngân hàng" : item.name
                lines.append("   \(idx + 1). \(itemName): \(CurrencyUtils.formatVnd(item.currentDebt))\(matStr)\(customStr)")
            }
            lines.append("   => Tổng dư nợ Ngân hàng: \(CurrencyUtils.formatVnd(result.bankInitialDebt))")
        }
        
        if !tctdItems.isEmpty {
            lines.append("2. KHỐI TỔ CHỨC TÍN DỤNG (Lãi suất áp dụng: \(tctdRate)%/năm):")
            for (idx, item) in tctdItems.enumerated() {
                let rate = item.customRate ?? tctdRate
                let customStr = item.customRate != nil ? " (Lãi riêng: \(rate)%)" : ""
                let matStr = !item.maturityDate.isEmpty ? " - Đáo hạn: \(item.maturityDate)" : ""
                let itemName = item.name.isEmpty ? "Tổ chức tín dụng" : item.name
                lines.append("   \(idx + 1). \(itemName): \(CurrencyUtils.formatVnd(item.currentDebt))\(matStr)\(customStr)")
            }
            lines.append("   => Tổng dư nợ TCTD: \(CurrencyUtils.formatVnd(result.tctdInitialDebt))")
        }
        
        lines.append("--------------------------------------")
        lines.append("📈 TỔNG HỢP DANH MỤC:")
        lines.append("• Tổng dư nợ tất cả các nguồn: \(CurrencyUtils.formatVnd(result.totalInitialDebt))")
        lines.append("  (\(CurrencyUtils.amountToWords(Int64(result.totalInitialDebt))))")
        let monthlyW = String(format: "%.2f", result.weightedAverageRate / 12.0)
        lines.append("• Lãi suất bình quân gia quyền: \(result.weightedAverageRate)%/năm (~ \(monthlyW)%/tháng)")
        lines.append("• Thời gian trả nợ đến khoản đáo hạn cuối: \(result.totalMonths) tháng")
        lines.append("--------------------------------------")
        lines.append("💵 DỰ TOÁN NGHĨA VỤ THANH TOÁN:")
        lines.append("• Số tiền trả tháng đầu tiên: \(CurrencyUtils.formatVnd(result.firstMonthPayment))")
        lines.append("• Số tiền trả cao nhất/tháng: \(CurrencyUtils.formatVnd(result.maxMonthPayment))")
        lines.append("• Tổng tiền lãi dự kiến cả chu kỳ: \(CurrencyUtils.formatVnd(result.totalInterest))")
        lines.append("  - Lãi Ngân hàng: \(CurrencyUtils.formatVnd(result.bankTotalInterest))")
        lines.append("  - Lãi Tổ chức tín dụng: \(CurrencyUtils.formatVnd(result.tctdTotalInterest))")
        lines.append("• Tổng cộng tiền gốc & lãi cần trả: \(CurrencyUtils.formatVnd(result.totalPayment))")
        lines.append("--------------------------------------")
        lines.append("💡 Khuyến nghị tài chính:")
        if result.tctdInitialDebt > 0 {
            lines.append("👉 Khoản nợ TCTD có lãi suất cao (\(tctdRate)%/năm). Em khuyên Anh/Chị nên ưu tiên tất toán sớm hoặc xem xét phương án vay Ngân hàng để gom nợ, giúp giảm áp lực tài chính hàng tháng!")
        } else {
            lines.append("👉 Danh mục nợ hiện tại được quản lý tốt với lãi suất ưu đãi từ Ngân hàng.")
        }
        return lines.joined(separator: "\n")
    }
}
