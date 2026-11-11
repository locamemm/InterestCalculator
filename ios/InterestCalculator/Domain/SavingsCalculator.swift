//
//  SavingsCalculator.swift
//  InterestCalculator
//

import Foundation

enum SavingsCalculator {
    
    static func calculate(
        depositAmount: Double,
        termMonths: Int,
        annualInterestRate: Double,
        paymentType: SavingsPaymentType = .endOfTerm,
        isAutoRenewCompounding: Bool = false,
        startDateStr: String = DateTimeUtils.todayString(),
        periodMonths: Int? = nil
    ) -> SavingsCalculationResult {
        let actualPeriodMonths = periodMonths ?? termMonths
        
        if depositAmount <= 0 || termMonths <= 0 {
            return SavingsCalculationResult(
                principalAmount: 0.0,
                totalInterest: 0.0,
                totalMaturityAmount: 0.0,
                effectiveAnnualRate: 0.0,
                schedule: []
            )
        }
        
        var schedule: [SavingsScheduleItem] = []
        var totalInterest = 0.0
        let maturityDate = DateTimeUtils.getPeriodDate(startDateStr, termMonths)
        
        switch paymentType {
        case .endOfTerm:
            let step = actualPeriodMonths > 0 ? min(actualPeriodMonths, termMonths) : termMonths
            if step >= termMonths {
                totalInterest = depositAmount * (annualInterestRate / 100.0) * (Double(termMonths) / 12.0)
                schedule.append(
                    SavingsScheduleItem(
                        period: 1,
                        payoutDate: maturityDate,
                        interestAmount: round(totalInterest),
                        principalAmount: round(depositAmount),
                        balanceAfter: round(depositAmount + totalInterest)
                    )
                )
            } else {
                var currentBalance = depositAmount
                var currentM = 0
                var periodIdx = 1
                
                while currentM < termMonths {
                    let nextM = min(currentM + step, termMonths)
                    let monthsInPeriod = nextM - currentM
                    let isLast = nextM == termMonths
                    
                    let periodInterest: Double
                    if isAutoRenewCompounding {
                        periodInterest = currentBalance * (annualInterestRate / 100.0) * (Double(monthsInPeriod) / 12.0)
                    } else {
                        periodInterest = depositAmount * (annualInterestRate / 100.0) * (Double(monthsInPeriod) / 12.0)
                    }
                    
                    totalInterest += periodInterest
                    let payoutDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                    
                    if isAutoRenewCompounding {
                        currentBalance += periodInterest
                        schedule.append(
                            SavingsScheduleItem(
                                period: periodIdx,
                                payoutDate: payoutDate,
                                interestAmount: round(periodInterest),
                                principalAmount: isLast ? round(currentBalance) : 0.0,
                                balanceAfter: round(currentBalance)
                            )
                        )
                    } else {
                        schedule.append(
                            SavingsScheduleItem(
                                period: periodIdx,
                                payoutDate: payoutDate,
                                interestAmount: round(periodInterest),
                                principalAmount: isLast ? round(depositAmount) : 0.0,
                                balanceAfter: isLast ? 0.0 : round(depositAmount)
                            )
                        )
                    }
                    periodIdx += 1
                    currentM = nextM
                }
            }
            
        case .monthly:
            let monthlyInterest = depositAmount * ((annualInterestRate / 100.0) / 12.0)
            for month in 1...termMonths {
                totalInterest += monthlyInterest
                let payoutDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                let isLast = month == termMonths
                
                schedule.append(
                    SavingsScheduleItem(
                        period: month,
                        payoutDate: payoutDate,
                        interestAmount: round(monthlyInterest),
                        principalAmount: isLast ? round(depositAmount) : 0.0,
                        balanceAfter: isLast ? 0.0 : round(depositAmount)
                    )
                )
            }
            
        case .quarterly:
            let quarterlyInterest = depositAmount * ((annualInterestRate / 100.0) / 4.0)
            let totalQuarters = max(1, termMonths / 3)
            
            for q in 1...totalQuarters {
                totalInterest += quarterlyInterest
                let payoutDate = DateTimeUtils.getPeriodDate(startDateStr, q * 3)
                let isLast = q == totalQuarters
                
                schedule.append(
                    SavingsScheduleItem(
                        period: q,
                        payoutDate: payoutDate,
                        interestAmount: round(quarterlyInterest),
                        principalAmount: isLast ? round(depositAmount) : 0.0,
                        balanceAfter: isLast ? 0.0 : round(depositAmount)
                    )
                )
            }
            
        case .upfront:
            totalInterest = depositAmount * (annualInterestRate / 100.0) * (Double(termMonths) / 12.0)
            schedule.append(
                SavingsScheduleItem(
                    period: 1,
                    payoutDate: startDateStr,
                    interestAmount: round(totalInterest),
                    principalAmount: 0.0,
                    balanceAfter: round(depositAmount)
                )
            )
            schedule.append(
                SavingsScheduleItem(
                    period: 2,
                    payoutDate: maturityDate,
                    interestAmount: 0.0,
                    principalAmount: round(depositAmount),
                    balanceAfter: 0.0
                )
            )
        }
        
        let totalMaturity = depositAmount + totalInterest
        let effectiveRate: Double
        if depositAmount > 0 && termMonths > 0 {
            effectiveRate = (totalInterest / depositAmount) * (12.0 / Double(termMonths)) * 100.0
        } else {
            effectiveRate = annualInterestRate
        }
        
        return SavingsCalculationResult(
            principalAmount: depositAmount,
            totalInterest: round(totalInterest),
            totalMaturityAmount: round(totalMaturity),
            effectiveAnnualRate: effectiveRate,
            schedule: schedule
        )
    }
    
    static func generateConsultationText(
        customerName: String,
        depositAmount: Double,
        termMonths: Int,
        interestRate: Double,
        paymentType: SavingsPaymentType,
        isAutoRenewCompounding: Bool,
        result: SavingsCalculationResult,
        startDateStr: String = DateTimeUtils.todayString(),
        periodMonths: Int? = nil
    ) -> String {
        let actualPeriodMonths = periodMonths ?? termMonths
        let name = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Quý khách" : customerName
        
        var lines: [String] = []
        lines.append("🏦 BẢNG DỰ TÍNH TIỀN GỬI TIẾT KIỆM - TƯ VẤN NGÂN HÀNG")
        lines.append("Kính gửi: Anh/Chị \(name)")
        lines.append("--------------------------------------")
        lines.append("• Số tiền gửi: \(CurrencyUtils.formatVnd(depositAmount))")
        lines.append("  (\(CurrencyUtils.amountToWords(Int64(depositAmount))))")
        lines.append("• Thời hạn gửi: \(termMonths) tháng")
        if actualPeriodMonths != termMonths {
            lines.append("• Kỳ hạn: \(actualPeriodMonths) tháng")
        }
        if !startDateStr.isEmpty {
            lines.append("• Ngày gửi tiền: \(startDateStr)")
            lines.append("• Ngày đáo hạn dự kiến: \(DateTimeUtils.getPeriodDate(startDateStr, termMonths))")
        }
        lines.append("• Lãi suất huy động: \(interestRate)%/năm")
        lines.append("• Hình thức trả lãi: \(paymentType.title)")
        if paymentType == .endOfTerm && isAutoRenewCompounding {
            lines.append("  (Chế độ lãi kép: Tự động quay vòng nhập gốc)")
        }
        lines.append("--------------------------------------")
        lines.append("💰 DÒNG TIỀN LỢI NHUẬN DỰ KIẾN:")
        lines.append("• Tổng tiền lãi thực nhận: \(CurrencyUtils.formatVnd(result.totalInterest))")
        lines.append("• Tổng tiền nhận khi tất toán: \(CurrencyUtils.formatVnd(result.totalMaturityAmount))")
        if paymentType == .monthly {
            let monthly = termMonths > 0 ? result.totalInterest / Double(termMonths) : 0.0
            lines.append("• Tiền lãi nhận định kỳ mỗi tháng: \(CurrencyUtils.formatVnd(monthly))")
        } else if paymentType == .quarterly {
            let quarterly = result.schedule.first?.interestAmount ?? 0.0
            lines.append("• Tiền lãi nhận định kỳ mỗi quý: \(CurrencyUtils.formatVnd(quarterly))")
        }
        lines.append("--------------------------------------")
        lines.append("🛡️ Tiền gửi của Anh/Chị được bảo đảm an toàn 100% theo quy định của Ngân hàng Nhà nước.")
        lines.append("Em luôn sẵn sàng hỗ trợ mở sổ tại quầy hoặc hướng dẫn gửi online với lãi suất tốt nhất!")
        return lines.joined(separator: "\n")
    }
}

