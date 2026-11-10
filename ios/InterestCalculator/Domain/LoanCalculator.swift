//
//  LoanCalculator.swift
//  InterestCalculator
//

import Foundation

enum LoanCalculator {
    
    static func calculate(
        principalAmount: Double,
        termMonths: Int,
        baseInterestRate: Double,
        hasPreferential: Bool = false,
        preferentialMonths: Int = 0,
        preferentialRate: Double? = nil,
        gracePeriodMonths: Int = 0,
        method: LoanMethod = .reducingBalance,
        startDateStr: String = DateTimeUtils.todayString(),
        repaymentPeriodMonths: Int = 1
    ) -> LoanCalculationResult {
        let prefRate = preferentialRate ?? baseInterestRate
        
        if principalAmount <= 0 || termMonths <= 0 {
            return LoanCalculationResult(
                totalPrincipal: 0.0,
                totalInterest: 0.0,
                totalPayment: 0.0,
                firstMonthPayment: 0.0,
                lastMonthPayment: 0.0,
                maxMonthPayment: 0.0,
                schedule: []
            )
        }
        
        var schedule: [RepaymentScheduleItem] = []
        var remainingBalance = principalAmount
        var totalInterest = 0.0
        let safeGraceMonths = min(gracePeriodMonths, termMonths - 1)
        let repayMonths = termMonths - safeGraceMonths
        
        switch method {
        case .endOfTerm:
            var calculatedTotalInterest = 0.0
            for month in 1...termMonths {
                let currentRate = (hasPreferential && month <= preferentialMonths) ? prefRate : baseInterestRate
                let monthlyRate = (currentRate / 100.0) / 12.0
                calculatedTotalInterest += principalAmount * monthlyRate
            }
            
            let step = repaymentPeriodMonths > 0 ? min(repaymentPeriodMonths, termMonths) : termMonths
            if step >= termMonths {
                let maturityDate = DateTimeUtils.getPeriodDate(startDateStr, termMonths)
                schedule.append(
                    RepaymentScheduleItem(
                        period: 1,
                        paymentDate: maturityDate,
                        principalPayment: round(principalAmount),
                        interestPayment: round(calculatedTotalInterest),
                        totalPayment: round(principalAmount + calculatedTotalInterest),
                        remainingBalance: 0.0
                    )
                )
            } else {
                var currentM = 0
                var periodIdx = 1
                var accumulatedInterest = 0.0
                while currentM < termMonths {
                    let nextM = min(currentM + step, termMonths)
                    var periodInterest = 0.0
                    for m in (currentM + 1)...nextM {
                        let currentRate = (hasPreferential && m <= preferentialMonths) ? prefRate : baseInterestRate
                        periodInterest += principalAmount * ((currentRate / 100.0) / 12.0)
                    }
                    accumulatedInterest += periodInterest
                    let isLast = nextM == termMonths
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                    let pPayment = isLast ? principalAmount : 0.0
                    let iPayment = isLast ? accumulatedInterest : 0.0
                    let remBal = isLast ? 0.0 : principalAmount
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: periodIdx,
                            paymentDate: paymentDate,
                            principalPayment: round(pPayment),
                            interestPayment: round(iPayment),
                            totalPayment: round(pPayment + iPayment),
                            remainingBalance: round(remBal)
                        )
                    )
                    periodIdx += 1
                    currentM = nextM
                }
            }
            totalInterest = calculatedTotalInterest
            
        case .reducingBalance:
            let step = repaymentPeriodMonths > 0 ? min(repaymentPeriodMonths, termMonths) : 1
            if step <= 1 {
                let fixedPrincipalPerMonth = repayMonths > 0 ? principalAmount / Double(repayMonths) : 0.0
                
                for month in 1...termMonths {
                    let currentRate = (hasPreferential && month <= preferentialMonths) ? prefRate : baseInterestRate
                    let monthlyRate = (currentRate / 100.0) / 12.0
                    let interestForMonth = remainingBalance * monthlyRate
                    
                    let principalForMonth: Double
                    if month <= safeGraceMonths {
                        principalForMonth = 0.0
                    } else if month == termMonths {
                        principalForMonth = remainingBalance
                    } else {
                        principalForMonth = min(fixedPrincipalPerMonth, remainingBalance)
                    }
                    
                    let totalForMonth = principalForMonth + interestForMonth
                    let newBalance = max(0.0, remainingBalance - principalForMonth)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: month,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForMonth),
                            interestPayment: round(interestForMonth),
                            totalPayment: round(totalForMonth),
                            remainingBalance: round(newBalance)
                        )
                    )
                    
                    totalInterest += interestForMonth
                    remainingBalance = newBalance
                }
            } else {
                var currentM = 0
                var periodIdx = 1
                let totalPeriods = (termMonths + step - 1) / step
                let fixedPrincipalPerPeriod = principalAmount / Double(totalPeriods)
                
                while currentM < termMonths {
                    let nextM = min(currentM + step, termMonths)
                    let isLast = nextM == termMonths
                    
                    var periodInterest = 0.0
                    for m in (currentM + 1)...nextM {
                        let currentRate = (hasPreferential && m <= preferentialMonths) ? prefRate : baseInterestRate
                        let monthlyRate = (currentRate / 100.0) / 12.0
                        periodInterest += remainingBalance * monthlyRate
                    }
                    
                    let principalForPeriod: Double
                    if nextM <= safeGraceMonths {
                        principalForPeriod = 0.0
                    } else if isLast {
                        principalForPeriod = remainingBalance
                    } else {
                        principalForPeriod = min(fixedPrincipalPerPeriod, remainingBalance)
                    }
                    
                    let totalForPeriod = principalForPeriod + periodInterest
                    let newBalance = max(0.0, remainingBalance - principalForPeriod)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: periodIdx,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForPeriod),
                            interestPayment: round(periodInterest),
                            totalPayment: round(totalForPeriod),
                            remainingBalance: round(newBalance)
                        )
                    )
                    periodIdx += 1
                    totalInterest += periodInterest
                    remainingBalance = newBalance
                    currentM = nextM
                }
            }
            
        case .annuity:
            let step = repaymentPeriodMonths > 0 ? min(repaymentPeriodMonths, termMonths) : 1
            if step <= 1 {
                for month in 1...termMonths {
                    let currentRate = (hasPreferential && month <= preferentialMonths) ? prefRate : baseInterestRate
                    let monthlyRate = (currentRate / 100.0) / 12.0
                    let remainingPeriods = termMonths - month + 1
                    
                    let pmt: Double
                    if monthlyRate > 0 {
                        let factor = pow(1.0 + monthlyRate, Double(remainingPeriods))
                        pmt = remainingBalance * (monthlyRate * factor) / (factor - 1.0)
                    } else {
                        pmt = remainingBalance / Double(remainingPeriods)
                    }
                    
                    let interestForMonth = remainingBalance * monthlyRate
                    let principalForMonth: Double
                    if month <= safeGraceMonths {
                        principalForMonth = 0.0
                    } else if month == termMonths {
                        principalForMonth = remainingBalance
                    } else {
                        principalForMonth = max(0.0, min(pmt - interestForMonth, remainingBalance))
                    }
                    
                    let totalForMonth = principalForMonth + interestForMonth
                    let newBalance = max(0.0, remainingBalance - principalForMonth)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: month,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForMonth),
                            interestPayment: round(interestForMonth),
                            totalPayment: round(totalForMonth),
                            remainingBalance: round(newBalance)
                        )
                    )
                    
                    totalInterest += interestForMonth
                    remainingBalance = newBalance
                }
            } else {
                var currentM = 0
                var periodIdx = 1
                let totalPeriods = (termMonths + step - 1) / step
                
                while currentM < termMonths {
                    let nextM = min(currentM + step, termMonths)
                    let isLast = nextM == termMonths
                    let remainingPeriods = totalPeriods - periodIdx + 1
                    let monthsInPeriod = nextM - currentM
                    
                    let currentRate = (hasPreferential && nextM <= preferentialMonths) ? prefRate : baseInterestRate
                    let periodRate = ((currentRate / 100.0) / 12.0) * Double(monthsInPeriod)
                    
                    let pmt: Double
                    if periodRate > 0 {
                        let factor = pow(1.0 + periodRate, Double(remainingPeriods))
                        pmt = remainingBalance * (periodRate * factor) / (factor - 1.0)
                    } else {
                        pmt = remainingBalance / Double(remainingPeriods)
                    }
                    
                    let interestForPeriod = remainingBalance * periodRate
                    let principalForPeriod: Double
                    if nextM <= safeGraceMonths {
                        principalForPeriod = 0.0
                    } else if isLast {
                        principalForPeriod = remainingBalance
                    } else {
                        principalForPeriod = max(0.0, min(pmt - interestForPeriod, remainingBalance))
                    }
                    
                    let totalForPeriod = principalForPeriod + interestForPeriod
                    let newBalance = max(0.0, remainingBalance - principalForPeriod)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: periodIdx,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForPeriod),
                            interestPayment: round(interestForPeriod),
                            totalPayment: round(totalForPeriod),
                            remainingBalance: round(newBalance)
                        )
                    )
                    periodIdx += 1
                    totalInterest += interestForPeriod
                    remainingBalance = newBalance
                    currentM = nextM
                }
            }
            
        case .flatRate:
            let step = repaymentPeriodMonths > 0 ? min(repaymentPeriodMonths, termMonths) : 1
            if step <= 1 {
                let monthlyPrincipal = principalAmount / Double(termMonths)
                
                for month in 1...termMonths {
                    let currentRate = (hasPreferential && month <= preferentialMonths) ? prefRate : baseInterestRate
                    let interestForMonth = principalAmount * ((currentRate / 100.0) / 12.0)
                    let principalForMonth: Double
                    if month <= safeGraceMonths {
                        principalForMonth = 0.0
                    } else if month == termMonths {
                        principalForMonth = remainingBalance
                    } else {
                        principalForMonth = min(monthlyPrincipal, remainingBalance)
                    }
                    
                    let totalForMonth = principalForMonth + interestForMonth
                    let newBalance = max(0.0, remainingBalance - principalForMonth)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: month,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForMonth),
                            interestPayment: round(interestForMonth),
                            totalPayment: round(totalForMonth),
                            remainingBalance: round(newBalance)
                        )
                    )
                    
                    totalInterest += interestForMonth
                    remainingBalance = newBalance
                }
            } else {
                var currentM = 0
                var periodIdx = 1
                let totalPeriods = (termMonths + step - 1) / step
                let principalPerPeriod = principalAmount / Double(totalPeriods)
                
                while currentM < termMonths {
                    let nextM = min(currentM + step, termMonths)
                    let isLast = nextM == termMonths
                    let monthsInPeriod = nextM - currentM
                    
                    let currentRate = (hasPreferential && nextM <= preferentialMonths) ? prefRate : baseInterestRate
                    let interestForPeriod = principalAmount * ((currentRate / 100.0) / 12.0) * Double(monthsInPeriod)
                    let principalForPeriod: Double
                    if nextM <= safeGraceMonths {
                        principalForPeriod = 0.0
                    } else if isLast {
                        principalForPeriod = remainingBalance
                    } else {
                        principalForPeriod = min(principalPerPeriod, remainingBalance)
                    }
                    
                    let totalForPeriod = principalForPeriod + interestForPeriod
                    let newBalance = max(0.0, remainingBalance - principalForPeriod)
                    let paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                    
                    schedule.append(
                        RepaymentScheduleItem(
                            period: periodIdx,
                            paymentDate: paymentDate,
                            principalPayment: round(principalForPeriod),
                            interestPayment: round(interestForPeriod),
                            totalPayment: round(totalForPeriod),
                            remainingBalance: round(newBalance)
                        )
                    )
                    periodIdx += 1
                    totalInterest += interestForPeriod
                    remainingBalance = newBalance
                    currentM = nextM
                }
            }
        }
        
        let firstMonthPayment = schedule.first?.totalPayment ?? 0.0
        let lastMonthPayment = schedule.last?.totalPayment ?? 0.0
        let maxMonthPayment = schedule.map { $0.totalPayment }.max() ?? 0.0
        let totalPayment = principalAmount + totalInterest
        
        return LoanCalculationResult(
            totalPrincipal: principalAmount,
            totalInterest: round(totalInterest),
            totalPayment: round(totalPayment),
            firstMonthPayment: firstMonthPayment,
            lastMonthPayment: lastMonthPayment,
            maxMonthPayment: maxMonthPayment,
            schedule: schedule
        )
    }
    
    static func generateConsultationText(
        customerName: String,
        principalAmount: Double,
        termMonths: Int,
        baseRate: Double,
        hasPreferential: Bool,
        preferentialMonths: Int,
        preferentialRate: Double,
        method: LoanMethod,
        result: LoanCalculationResult,
        startDateStr: String = DateTimeUtils.todayString(),
        repaymentPeriodMonths: Int = 1
    ) -> String {
        let name = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Quý khách" : customerName
        var lines: [String] = []
        lines.append("🏦 BẢNG DỰ TOÁN KHOẢN VAY - TƯ VẤN NGÂN HÀNG")
        lines.append("Kính gửi: Anh/Chị \(name)")
        lines.append("--------------------------------------")
        lines.append("• Số tiền vay: \(CurrencyUtils.formatVnd(principalAmount))")
        lines.append("  (\(CurrencyUtils.amountToWords(Int64(principalAmount))))")
        let years = termMonths / 12
        let remMonths = termMonths % 12
        let yearsStr = "\(years) năm\(remMonths > 0 ? " \(remMonths) tháng" : "")"
        lines.append("• Thời hạn vay: \(termMonths) tháng (\(yearsStr))")
        if repaymentPeriodMonths > 1 {
            lines.append("• Kỳ hạn trả nợ: \(repaymentPeriodMonths) tháng/kỳ")
        }
        if !startDateStr.isEmpty {
            lines.append("• Ngày giải ngân dự kiến: \(startDateStr)")
            lines.append("• Ngày đáo hạn / tất toán: \(DateTimeUtils.getPeriodDate(startDateStr, termMonths))")
        }
        lines.append("• Phương thức tính: \(method.title)")
        
        if hasPreferential {
            let prefMonthly = String(format: "%.2f", preferentialRate / 12.0).trimmingTrailingZeros()
            let baseMonthly = String(format: "%.2f", baseRate / 12.0).trimmingTrailingZeros()
            lines.append("• Lãi suất ưu đãi: \(preferentialRate)%/năm (~ \(prefMonthly)%/tháng, trong \(preferentialMonths) tháng đầu)")
            lines.append("• Lãi suất sau ưu đãi: \(baseRate)%/năm (~ \(baseMonthly)%/tháng)")
        } else {
            let baseMonthly = String(format: "%.2f", baseRate / 12.0).trimmingTrailingZeros()
            lines.append("• Lãi suất áp dụng: \(baseRate)%/năm (~ \(baseMonthly)%/tháng)")
        }
        
        lines.append("--------------------------------------")
        lines.append("📊 DÒNG TIỀN THANH TOÁN DỰ KIẾN:")
        lines.append("• Số tiền trả kỳ đầu tiên: \(CurrencyUtils.formatVnd(result.firstMonthPayment))")
        if result.firstMonthPayment != result.lastMonthPayment {
            lines.append("• Số tiền trả kỳ cuối cùng: \(CurrencyUtils.formatVnd(result.lastMonthPayment))")
        }
        lines.append("• Số tiền trả cao nhất/kỳ: \(CurrencyUtils.formatVnd(result.maxMonthPayment))")
        lines.append("• Tổng tiền lãi dự kiến: \(CurrencyUtils.formatVnd(result.totalInterest))")
        lines.append("• Tổng gốc và lãi: \(CurrencyUtils.formatVnd(result.totalPayment))")
        lines.append("--------------------------------------")
        lines.append("💡 Lưu ý: Bảng tính mang tính chất tham khảo, lãi suất thực tế sẽ được áp dụng theo quy định của Ngân hàng tại thời điểm giải ngân.")
        lines.append("Em luôn sẵn sàng hỗ trợ Anh/Chị hoàn tất hồ sơ vay nhanh nhất!")
        return lines.joined(separator: "\n")
    }
}

private extension String {
    func trimmingTrailingZeros() -> String {
        var str = self
        if str.contains(".") {
            while str.hasSuffix("0") { str.removeLast() }
            if str.hasSuffix(".") { str.removeLast() }
        }
        return str
    }
}
