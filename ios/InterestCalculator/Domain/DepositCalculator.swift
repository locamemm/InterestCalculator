//
//  DepositCalculator.swift
//  InterestCalculator
//

import Foundation

enum DepositCalculator {
    
    private final class ItemTracker {
        let item: DepositSourceItem
        let effectiveRate: Double
        let termMonths: Int
        var currentBalance: Double
        var accumulatedInterest: Double = 0.0
        var isSettled: Bool = false
        
        init(item: DepositSourceItem, effectiveRate: Double, termMonths: Int) {
            self.item = item
            self.effectiveRate = effectiveRate
            self.termMonths = termMonths
            self.currentBalance = item.principalAmount
        }
    }
    
    static func calculate(
        items: [DepositSourceItem],
        startDateStr: String = DateTimeUtils.todayString(),
        counterRate: Double = 5.2,
        onlineRate: Double = 6.0,
        paymentType: SavingsPaymentType = .endOfTerm,
        isCompounding: Bool = false
    ) -> DepositScheduleResult {
        let validItems = items.filter { $0.principalAmount > 0 }
        if validItems.isEmpty {
            return DepositScheduleResult(
                totalPrincipal: 0.0,
                counterPrincipal: 0.0,
                onlinePrincipal: 0.0,
                totalInterest: 0.0,
                counterTotalInterest: 0.0,
                onlineTotalInterest: 0.0,
                totalMaturityAmount: 0.0,
                firstMonthPayout: 0.0,
                maxMonthPayout: 0.0,
                weightedAverageRate: 0.0,
                totalMonths: 0,
                schedule: []
            )
        }
        
        let trackers: [ItemTracker] = validItems.map { item in
            let rate = item.customRate ?? (item.sourceType == .counter ? counterRate : onlineRate)
            let targetMaturity = !item.maturityDate.isEmpty ? item.maturityDate : DateTimeUtils.addMonths(startDateStr, 12)
            let months = max(1, DateTimeUtils.monthsBetween(startDateStr, targetMaturity))
            return ItemTracker(
                item: item,
                effectiveRate: rate,
                termMonths: months
            )
        }
        
        let totalPrincipal = validItems.reduce(0.0) { $0 + $1.principalAmount }
        let counterPrincipal = validItems.filter { $0.sourceType == .counter }.reduce(0.0) { $0 + $1.principalAmount }
        let onlinePrincipal = validItems.filter { $0.sourceType == .online }.reduce(0.0) { $0 + $1.principalAmount }
        
        let weightedRate: Double
        if totalPrincipal > 0 {
            weightedRate = trackers.reduce(0.0) { $0 + ($1.item.principalAmount * $1.effectiveRate) } / totalPrincipal
        } else {
            weightedRate = 0.0
        }
        
        let maxMonths = trackers.map { $0.termMonths }.max() ?? 1
        var schedule: [DepositPeriodBreakdown] = []
        
        var totalInterestAccumulated = 0.0
        var counterInterestAccumulated = 0.0
        var onlineInterestAccumulated = 0.0
        
        for m in 1...maxMonths {
            let payoutDate = DateTimeUtils.getPeriodDate(startDateStr, m)
            
            var periodInterest = 0.0
            var periodPrincipal = 0.0
            var periodCounterInterest = 0.0
            var periodOnlineInterest = 0.0
            
            var itemPayouts: [DepositItemPeriodPayout] = []
            
            for tracker in trackers {
                if m > tracker.termMonths || tracker.isSettled {
                    continue
                }
                
                let monthlyRate = (tracker.effectiveRate / 100.0) / 12.0
                let isLastMonth = (m == tracker.termMonths)
                
                var interestPayout = 0.0
                var principalPayout = 0.0
                var remainingAfter = tracker.currentBalance
                
                switch paymentType {
                case .endOfTerm:
                    if isCompounding {
                        let mInt = tracker.currentBalance * monthlyRate
                        tracker.currentBalance += mInt
                        tracker.accumulatedInterest += mInt
                    } else {
                        let mInt = tracker.item.principalAmount * monthlyRate
                        tracker.accumulatedInterest += mInt
                    }
                    
                    if isLastMonth {
                        interestPayout = tracker.accumulatedInterest
                        principalPayout = tracker.item.principalAmount
                        remainingAfter = 0.0
                        tracker.isSettled = true
                    } else {
                        remainingAfter = isCompounding ? tracker.currentBalance : tracker.item.principalAmount
                    }
                    
                case .monthly:
                    interestPayout = tracker.item.principalAmount * monthlyRate
                    if isLastMonth {
                        principalPayout = tracker.item.principalAmount
                        remainingAfter = 0.0
                        tracker.isSettled = true
                    } else {
                        remainingAfter = tracker.item.principalAmount
                    }
                    
                case .quarterly:
                    tracker.accumulatedInterest += tracker.item.principalAmount * monthlyRate
                    if m % 3 == 0 || isLastMonth {
                        interestPayout = tracker.accumulatedInterest
                        tracker.accumulatedInterest = 0.0
                    }
                    if isLastMonth {
                        principalPayout = tracker.item.principalAmount
                        remainingAfter = 0.0
                        tracker.isSettled = true
                    } else {
                        remainingAfter = tracker.item.principalAmount
                    }
                    
                case .upfront:
                    if m == 1 {
                        interestPayout = tracker.item.principalAmount * (tracker.effectiveRate / 100.0) * (Double(tracker.termMonths) / 12.0)
                    }
                    if isLastMonth {
                        principalPayout = tracker.item.principalAmount
                        remainingAfter = 0.0
                        tracker.isSettled = true
                    } else {
                        remainingAfter = tracker.item.principalAmount
                    }
                }
                
                periodInterest += interestPayout
                periodPrincipal += principalPayout
                
                if tracker.item.sourceType == .counter {
                    periodCounterInterest += interestPayout
                } else {
                    periodOnlineInterest += interestPayout
                }
                
                let itemName = tracker.item.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? tracker.item.sourceType.title : tracker.item.name
                itemPayouts.append(
                    DepositItemPeriodPayout(
                        itemId: tracker.item.id,
                        itemName: itemName,
                        sourceType: tracker.item.sourceType,
                        interestPayout: round(interestPayout),
                        principalPayout: round(principalPayout),
                        totalPayout: round(interestPayout + principalPayout),
                        remainingBalance: round(remainingAfter)
                    )
                )
            }
            
            let totalRemaining = trackers.filter { !$0.isSettled }.reduce(0.0) { total, tr in
                total + (paymentType == .endOfTerm && isCompounding ? tr.currentBalance : tr.item.principalAmount)
            }
            let periodTotal = periodInterest + periodPrincipal
            
            totalInterestAccumulated += periodInterest
            counterInterestAccumulated += periodCounterInterest
            onlineInterestAccumulated += periodOnlineInterest
            
            schedule.append(
                DepositPeriodBreakdown(
                    period: m,
                    payoutDate: payoutDate,
                    interestPayout: round(periodInterest),
                    principalPayout: round(periodPrincipal),
                    totalPayout: round(periodTotal),
                    remainingBalance: round(totalRemaining),
                    counterInterestPayout: round(periodCounterInterest),
                    onlineInterestPayout: round(periodOnlineInterest),
                    itemBreakdowns: itemPayouts
                )
            )
        }
        
        let firstMonth = schedule.first?.totalPayout ?? 0.0
        let maxMonth = schedule.map { $0.totalPayout }.max() ?? 0.0
        
        return DepositScheduleResult(
            totalPrincipal: round(totalPrincipal),
            counterPrincipal: round(counterPrincipal),
            onlinePrincipal: round(onlinePrincipal),
            totalInterest: round(totalInterestAccumulated),
            counterTotalInterest: round(counterInterestAccumulated),
            onlineTotalInterest: round(onlineInterestAccumulated),
            totalMaturityAmount: round(totalPrincipal + totalInterestAccumulated),
            firstMonthPayout: firstMonth,
            maxMonthPayout: maxMonth,
            weightedAverageRate: round(weightedRate * 100.0) / 100.0,
            totalMonths: maxMonths,
            schedule: schedule
        )
    }
    
    static func generateDepositConsultationText(
        customerName: String,
        startDateStr: String,
        counterRate: Double,
        onlineRate: Double,
        paymentType: SavingsPaymentType,
        isCompounding: Bool,
        items: [DepositSourceItem],
        result: DepositScheduleResult
    ) -> String {
        let name = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Quý khách" : customerName
        var lines: [String] = []
        lines.append("💰 BẢNG TỔNG HỢP TIỀN GỬI & LỢI NHUẬN ĐA SỔ")
        lines.append("Kính gửi: Anh/Chị \(name)")
        lines.append("• Mốc thời gian tổng hợp: \(startDateStr)")
        lines.append("• Hình thức lĩnh lãi: \(paymentType.title)\(paymentType == .endOfTerm && isCompounding ? " (Lãi kép nhập gốc)" : "")")
        lines.append("--------------------------------------")
        lines.append("📊 DANH SÁCH CÁC SỔ TIẾT KIỆM HIỆN CÓ:")
        
        let counterItems = items.filter { $0.sourceType == .counter && $0.principalAmount > 0 }
        let onlineItems = items.filter { $0.sourceType == .online && $0.principalAmount > 0 }
        
        if !counterItems.isEmpty {
            lines.append("1. GỬI TẠI QUẦY (Lãi suất chung: \(counterRate)%/năm):")
            for (idx, item) in counterItems.enumerated() {
                let rate = item.customRate ?? counterRate
                let customStr = item.customRate != nil ? " (Lãi riêng: \(rate)%)" : ""
                let matStr = !item.maturityDate.isEmpty ? " - Đáo hạn: \(item.maturityDate)" : ""
                let itemName = item.name.isEmpty ? "Sổ tại quầy" : item.name
                lines.append("   \(idx + 1). \(itemName): \(CurrencyUtils.formatVnd(item.principalAmount))\(matStr)\(customStr)")
            }
            lines.append("   => Tổng gốc tại quầy: \(CurrencyUtils.formatVnd(result.counterPrincipal))")
        }
        
        if !onlineItems.isEmpty {
            lines.append("2. GỬI TRỰC TUYẾN / ONLINE (Lãi suất chung: \(onlineRate)%/năm):")
            for (idx, item) in onlineItems.enumerated() {
                let rate = item.customRate ?? onlineRate
                let customStr = item.customRate != nil ? " (Lãi riêng: \(rate)%)" : ""
                let matStr = !item.maturityDate.isEmpty ? " - Đáo hạn: \(item.maturityDate)" : ""
                let itemName = item.name.isEmpty ? "Sổ Online" : item.name
                lines.append("   \(idx + 1). \(itemName): \(CurrencyUtils.formatVnd(item.principalAmount))\(matStr)\(customStr)")
            }
            lines.append("   => Tổng gốc Online: \(CurrencyUtils.formatVnd(result.onlinePrincipal))")
        }
        
        lines.append("--------------------------------------")
        lines.append("📈 TỔNG HỢP DANH MỤC TIỀN GỬI:")
        lines.append("• Tổng tiền gốc gửi tất cả nguồn: \(CurrencyUtils.formatVnd(result.totalPrincipal))")
        lines.append("  (\(CurrencyUtils.amountToWords(Int64(result.totalPrincipal))))")
        let monthlyW = String(format: "%.2f", result.weightedAverageRate / 12.0)
        lines.append("• Lãi suất bình quân gia quyền: \(result.weightedAverageRate)%/năm (~ \(monthlyW)%/tháng)")
        lines.append("• Thời gian đến sổ đáo hạn cuối: \(result.totalMonths) tháng")
        lines.append("--------------------------------------")
        lines.append("💵 DỰ TOÁN LỢI NHUẬN NHẬN ĐƯỢC:")
        lines.append("• Tổng tiền lãi dự kiến cả chu kỳ: \(CurrencyUtils.formatVnd(result.totalInterest))")
        lines.append("  - Lãi gửi tại quầy: \(CurrencyUtils.formatVnd(result.counterTotalInterest))")
        lines.append("  - Lãi gửi Online: \(CurrencyUtils.formatVnd(result.onlineTotalInterest))")
        lines.append("• Tổng gốc + lãi nhận khi tất toán hết: \(CurrencyUtils.formatVnd(result.totalMaturityAmount))")
        lines.append("--------------------------------------")
        lines.append("💡 Khuyến nghị tối ưu lợi nhuận:")
        if result.counterPrincipal > 0 && onlineRate > counterRate {
            lines.append("👉 Các sổ gửi Online đang có mức lãi suất ưu đãi hơn (\(onlineRate)%/năm so với \(counterRate)%/năm tại quầy). Khi các sổ tại quầy đáo hạn, Anh/Chị có thể cân nhắc chuyển sang gửi Online để tối đa hóa tiền lãi!")
        } else {
            lines.append("👉 Danh mục tiền gửi của Anh/Chị đang được phân bổ hiệu quả với mức sinh lời ổn định và an toàn.")
        }
        return lines.joined(separator: "\n")
    }
}
