package com.example.interestcalculator.domain

import com.example.interestcalculator.data.model.LoanCalculationResult
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.RepaymentScheduleItem
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

object LoanCalculator {

    fun calculate(
        principalAmount: Double,
        termMonths: Int,
        baseInterestRate: Double,
        hasPreferential: Boolean = false,
        preferentialMonths: Int = 0,
        preferentialRate: Double = baseInterestRate,
        gracePeriodMonths: Int = 0,
        method: LoanMethod = LoanMethod.REDUCING_BALANCE,
        startDateStr: String = DateTimeUtils.todayString(),
        repaymentPeriodMonths: Int = 1
    ): LoanCalculationResult {
        if (principalAmount <= 0 || termMonths <= 0) {
            return LoanCalculationResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
        }

        val schedule = mutableListOf<RepaymentScheduleItem>()
        var remainingBalance = principalAmount
        var totalInterest = 0.0
        val safeGraceMonths = min(gracePeriodMonths, termMonths - 1)
        val repayMonths = termMonths - safeGraceMonths

        when (method) {
            LoanMethod.END_OF_TERM -> {
                // Lãi cuối kỳ: Gốc và lãi trả toàn bộ một lần khi đáo hạn
                var calculatedTotalInterest = 0.0
                for (month in 1..termMonths) {
                    val currentRate = if (hasPreferential && month <= preferentialMonths) {
                        preferentialRate
                    } else {
                        baseInterestRate
                    }
                    val monthlyRate = (currentRate / 100.0) / 12.0
                    calculatedTotalInterest += principalAmount * monthlyRate
                }

                val step = if (repaymentPeriodMonths > 0) min(repaymentPeriodMonths, termMonths) else termMonths
                if (step >= termMonths) {
                    val maturityDate = DateTimeUtils.getPeriodDate(startDateStr, termMonths)
                    schedule.add(
                        RepaymentScheduleItem(
                            period = 1,
                            paymentDate = maturityDate,
                            principalPayment = round(principalAmount),
                            interestPayment = round(calculatedTotalInterest),
                            totalPayment = round(principalAmount + calculatedTotalInterest),
                            remainingBalance = 0.0
                        )
                    )
                } else {
                    var currentM = 0
                    var periodIdx = 1
                    var accumulatedInterest = 0.0
                    while (currentM < termMonths) {
                        val nextM = min(currentM + step, termMonths)
                        var periodInterest = 0.0
                        for (m in (currentM + 1)..nextM) {
                            val currentRate = if (hasPreferential && m <= preferentialMonths) {
                                preferentialRate
                            } else {
                                baseInterestRate
                            }
                            periodInterest += principalAmount * ((currentRate / 100.0) / 12.0)
                        }
                        accumulatedInterest += periodInterest
                        val isLast = nextM == termMonths
                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)
                        val pPayment = if (isLast) principalAmount else 0.0
                        val iPayment = if (isLast) accumulatedInterest else 0.0
                        val remBal = if (isLast) 0.0 else principalAmount

                        schedule.add(
                            RepaymentScheduleItem(
                                period = periodIdx++,
                                paymentDate = paymentDate,
                                principalPayment = round(pPayment),
                                interestPayment = round(iPayment),
                                totalPayment = round(pPayment + iPayment),
                                remainingBalance = round(remBal)
                            )
                        )
                        currentM = nextM
                    }
                }
                totalInterest = calculatedTotalInterest
            }

            LoanMethod.REDUCING_BALANCE -> {
                val step = if (repaymentPeriodMonths > 0) min(repaymentPeriodMonths, termMonths) else 1
                if (step <= 1) {
                    val fixedPrincipalPerMonth = if (repayMonths > 0) principalAmount / repayMonths else 0.0

                    for (month in 1..termMonths) {
                        val currentRate = if (hasPreferential && month <= preferentialMonths) {
                            preferentialRate
                        } else {
                            baseInterestRate
                        }
                        val monthlyRate = (currentRate / 100.0) / 12.0
                        val interestForMonth = remainingBalance * monthlyRate

                        val principalForMonth = if (month <= safeGraceMonths) {
                            0.0
                        } else if (month == termMonths) {
                            remainingBalance // trả nốt số dư còn lại để dư nợ về 0
                        } else {
                            min(fixedPrincipalPerMonth, remainingBalance)
                        }

                        val totalForMonth = principalForMonth + interestForMonth
                        val newBalance = max(0.0, remainingBalance - principalForMonth)

                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                        schedule.add(
                            RepaymentScheduleItem(
                                period = month,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForMonth),
                                interestPayment = round(interestForMonth),
                                totalPayment = round(totalForMonth),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += interestForMonth
                        remainingBalance = newBalance
                    }
                } else {
                    // Trả theo kỳ hạn step tháng
                    var currentM = 0
                    var periodIdx = 1
                    val totalPeriods = (termMonths + step - 1) / step
                    val fixedPrincipalPerPeriod = principalAmount / totalPeriods

                    while (currentM < termMonths) {
                        val nextM = min(currentM + step, termMonths)
                        val isLast = nextM == termMonths

                        var periodInterest = 0.0
                        for (m in (currentM + 1)..nextM) {
                            val currentRate = if (hasPreferential && m <= preferentialMonths) {
                                preferentialRate
                            } else {
                                baseInterestRate
                            }
                            val monthlyRate = (currentRate / 100.0) / 12.0
                            periodInterest += remainingBalance * monthlyRate
                        }

                        val principalForPeriod = if (nextM <= safeGraceMonths) {
                            0.0
                        } else if (isLast) {
                            remainingBalance
                        } else {
                            min(fixedPrincipalPerPeriod, remainingBalance)
                        }

                        val totalForPeriod = principalForPeriod + periodInterest
                        val newBalance = max(0.0, remainingBalance - principalForPeriod)
                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)

                        schedule.add(
                            RepaymentScheduleItem(
                                period = periodIdx++,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForPeriod),
                                interestPayment = round(periodInterest),
                                totalPayment = round(totalForPeriod),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += periodInterest
                        remainingBalance = newBalance
                        currentM = nextM
                    }
                }
            }

            LoanMethod.ANNUITY -> {
                // Trả góp đều (Gốc + Lãi cố định mỗi kỳ)
                val step = if (repaymentPeriodMonths > 0) min(repaymentPeriodMonths, termMonths) else 1
                if (step <= 1) {
                    for (month in 1..termMonths) {
                        val currentRate = if (hasPreferential && month <= preferentialMonths) {
                            preferentialRate
                        } else {
                            baseInterestRate
                        }
                        val monthlyRate = (currentRate / 100.0) / 12.0
                        val remainingPeriods = termMonths - month + 1

                        val pmt = if (monthlyRate > 0) {
                            val factor = (1 + monthlyRate).pow(remainingPeriods.toDouble())
                            remainingBalance * (monthlyRate * factor) / (factor - 1)
                        } else {
                            remainingBalance / remainingPeriods
                        }

                        val interestForMonth = remainingBalance * monthlyRate
                        var principalForMonth = if (month <= safeGraceMonths) {
                            0.0
                        } else if (month == termMonths) {
                            remainingBalance
                        } else {
                            max(0.0, min(pmt - interestForMonth, remainingBalance))
                        }

                        val totalForMonth = principalForMonth + interestForMonth
                        val newBalance = max(0.0, remainingBalance - principalForMonth)

                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                        schedule.add(
                            RepaymentScheduleItem(
                                period = month,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForMonth),
                                interestPayment = round(interestForMonth),
                                totalPayment = round(totalForMonth),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += interestForMonth
                        remainingBalance = newBalance
                    }
                } else {
                    var currentM = 0
                    var periodIdx = 1
                    val totalPeriods = (termMonths + step - 1) / step

                    while (currentM < termMonths) {
                        val nextM = min(currentM + step, termMonths)
                        val isLast = nextM == termMonths
                        val remainingPeriods = totalPeriods - periodIdx + 1
                        val monthsInPeriod = nextM - currentM

                        val currentRate = if (hasPreferential && nextM <= preferentialMonths) {
                            preferentialRate
                        } else {
                            baseInterestRate
                        }
                        val periodRate = ((currentRate / 100.0) / 12.0) * monthsInPeriod

                        val pmt = if (periodRate > 0) {
                            val factor = (1 + periodRate).pow(remainingPeriods.toDouble())
                            remainingBalance * (periodRate * factor) / (factor - 1)
                        } else {
                            remainingBalance / remainingPeriods
                        }

                        val interestForPeriod = remainingBalance * periodRate
                        val principalForPeriod = if (nextM <= safeGraceMonths) {
                            0.0
                        } else if (isLast) {
                            remainingBalance
                        } else {
                            max(0.0, min(pmt - interestForPeriod, remainingBalance))
                        }

                        val totalForPeriod = principalForPeriod + interestForPeriod
                        val newBalance = max(0.0, remainingBalance - principalForPeriod)
                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)

                        schedule.add(
                            RepaymentScheduleItem(
                                period = periodIdx++,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForPeriod),
                                interestPayment = round(interestForPeriod),
                                totalPayment = round(totalForPeriod),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += interestForPeriod
                        remainingBalance = newBalance
                        currentM = nextM
                    }
                }
            }

            LoanMethod.FLAT_RATE -> {
                // Lãi phẳng: Gốc đều, lãi cố định theo số tiền gốc ban đầu
                val step = if (repaymentPeriodMonths > 0) min(repaymentPeriodMonths, termMonths) else 1
                if (step <= 1) {
                    val monthlyPrincipal = principalAmount / termMonths

                    for (month in 1..termMonths) {
                        val currentRate = if (hasPreferential && month <= preferentialMonths) {
                            preferentialRate
                        } else {
                            baseInterestRate
                        }
                        val interestForMonth = principalAmount * ((currentRate / 100.0) / 12.0)
                        val principalForMonth = if (month <= safeGraceMonths) {
                            0.0
                        } else if (month == termMonths) {
                            remainingBalance
                        } else {
                            min(monthlyPrincipal, remainingBalance)
                        }

                        val totalForMonth = principalForMonth + interestForMonth
                        val newBalance = max(0.0, remainingBalance - principalForMonth)

                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                        schedule.add(
                            RepaymentScheduleItem(
                                period = month,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForMonth),
                                interestPayment = round(interestForMonth),
                                totalPayment = round(totalForMonth),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += interestForMonth
                        remainingBalance = newBalance
                    }
                } else {
                    var currentM = 0
                    var periodIdx = 1
                    val totalPeriods = (termMonths + step - 1) / step
                    val principalPerPeriod = principalAmount / totalPeriods

                    while (currentM < termMonths) {
                        val nextM = min(currentM + step, termMonths)
                        val isLast = nextM == termMonths
                        val monthsInPeriod = nextM - currentM

                        val currentRate = if (hasPreferential && nextM <= preferentialMonths) {
                            preferentialRate
                        } else {
                            baseInterestRate
                        }
                        val interestForPeriod = principalAmount * ((currentRate / 100.0) / 12.0) * monthsInPeriod
                        val principalForPeriod = if (nextM <= safeGraceMonths) {
                            0.0
                        } else if (isLast) {
                            remainingBalance
                        } else {
                            min(principalPerPeriod, remainingBalance)
                        }

                        val totalForPeriod = principalForPeriod + interestForPeriod
                        val newBalance = max(0.0, remainingBalance - principalForPeriod)
                        val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)

                        schedule.add(
                            RepaymentScheduleItem(
                                period = periodIdx++,
                                paymentDate = paymentDate,
                                principalPayment = round(principalForPeriod),
                                interestPayment = round(interestForPeriod),
                                totalPayment = round(totalForPeriod),
                                remainingBalance = round(newBalance)
                            )
                        )

                        totalInterest += interestForPeriod
                        remainingBalance = newBalance
                        currentM = nextM
                    }
                }
            }
        }

        val firstMonthPayment = schedule.firstOrNull()?.totalPayment ?: 0.0
        val lastMonthPayment = schedule.lastOrNull()?.totalPayment ?: 0.0
        val maxMonthPayment = schedule.maxOfOrNull { it.totalPayment } ?: 0.0
        val totalPayment = principalAmount + totalInterest

        return LoanCalculationResult(
            totalPrincipal = principalAmount,
            totalInterest = round(totalInterest),
            totalPayment = round(totalPayment),
            firstMonthPayment = firstMonthPayment,
            lastMonthPayment = lastMonthPayment,
            maxMonthPayment = maxMonthPayment,
            schedule = schedule
        )
    }

    /**
     * Tạo nội dung tư vấn soạn sẵn để gửi qua Zalo / SMS cho khách
     */
    fun generateConsultationText(
        customerName: String,
        principalAmount: Double,
        termMonths: Int,
        baseRate: Double,
        hasPreferential: Boolean,
        preferentialMonths: Int,
        preferentialRate: Double,
        method: LoanMethod,
        result: LoanCalculationResult,
        startDateStr: String = DateTimeUtils.todayString(),
        repaymentPeriodMonths: Int = 1
    ): String {
        val name = if (customerName.isNotBlank()) customerName else "Quý khách"
        val sb = StringBuilder()
        sb.appendLine("🏦 BẢNG DỰ TOÁN KHOẢN VAY - TƯ VẤN NGÂN HÀNG")
        sb.appendLine("Kính gửi: Anh/Chị $name")
        sb.appendLine("--------------------------------------")
        sb.appendLine("• Số tiền vay: ${CurrencyUtils.formatVnd(principalAmount)}")
        sb.appendLine("  (${CurrencyUtils.amountToWords(principalAmount.toLong())})")
        sb.appendLine("• Thời hạn vay: $termMonths tháng (${termMonths / 12} năm ${if (termMonths % 12 > 0) "${termMonths % 12} tháng" else ""})")
        if (repaymentPeriodMonths > 1) {
            sb.appendLine("• Kỳ hạn trả nợ: $repaymentPeriodMonths tháng/kỳ")
        }
        if (startDateStr.isNotBlank()) {
            sb.appendLine("• Ngày giải ngân dự kiến: $startDateStr")
            sb.appendLine("• Ngày đáo hạn / tất toán: ${DateTimeUtils.getPeriodDate(startDateStr, termMonths)}")
        }
        sb.appendLine("• Phương thức tính: ${method.title}")

        if (hasPreferential) {
            val prefMonthlyStr = String.format(java.util.Locale.US, "%.2f", preferentialRate / 12.0).trimEnd('0').trimEnd('.')
            val baseMonthlyStr = String.format(java.util.Locale.US, "%.2f", baseRate / 12.0).trimEnd('0').trimEnd('.')
            sb.appendLine("• Lãi suất ưu đãi: $preferentialRate%/năm (~ $prefMonthlyStr%/tháng, trong $preferentialMonths tháng đầu)")
            sb.appendLine("• Lãi suất sau ưu đãi: $baseRate%/năm (~ $baseMonthlyStr%/tháng)")
        } else {
            val baseMonthlyStr = String.format(java.util.Locale.US, "%.2f", baseRate / 12.0).trimEnd('0').trimEnd('.')
            sb.appendLine("• Lãi suất áp dụng: $baseRate%/năm (~ $baseMonthlyStr%/tháng)")
        }

        sb.appendLine("--------------------------------------")
        sb.appendLine("📊 DÒNG TIỀN THANH TOÁN DỰ KIẾN:")
        sb.appendLine("• Số tiền trả kỳ đầu tiên: ${CurrencyUtils.formatVnd(result.firstMonthPayment)}")
        if (result.firstMonthPayment != result.lastMonthPayment) {
            sb.appendLine("• Số tiền trả kỳ cuối cùng: ${CurrencyUtils.formatVnd(result.lastMonthPayment)}")
        }
        sb.appendLine("• Số tiền trả cao nhất/kỳ: ${CurrencyUtils.formatVnd(result.maxMonthPayment)}")
        sb.appendLine("• Tổng tiền lãi dự kiến: ${CurrencyUtils.formatVnd(result.totalInterest)}")
        sb.appendLine("• Tổng gốc và lãi: ${CurrencyUtils.formatVnd(result.totalPayment)}")
        sb.appendLine("--------------------------------------")
        sb.appendLine("💡 Lưu ý: Bảng tính mang tính chất tham khảo, lãi suất thực tế sẽ được áp dụng theo quy định của Ngân hàng tại thời điểm giải ngân.")
        sb.appendLine("Em luôn sẵn sàng hỗ trợ Anh/Chị hoàn tất hồ sơ vay nhanh nhất!")
        return sb.toString()
    }
}
