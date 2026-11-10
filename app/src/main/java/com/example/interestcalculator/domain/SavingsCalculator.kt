package com.example.interestcalculator.domain

import com.example.interestcalculator.data.model.SavingsCalculationResult
import com.example.interestcalculator.data.model.SavingsPaymentType
import com.example.interestcalculator.data.model.SavingsScheduleItem
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

object SavingsCalculator {

    fun calculate(
        depositAmount: Double,
        termMonths: Int,
        annualInterestRate: Double,
        paymentType: SavingsPaymentType = SavingsPaymentType.END_OF_TERM,
        isAutoRenewCompounding: Boolean = false,
        startDateStr: String = DateTimeUtils.todayString(),
        periodMonths: Int = termMonths
    ): SavingsCalculationResult {
        if (depositAmount <= 0 || termMonths <= 0) {
            return SavingsCalculationResult(0.0, 0.0, 0.0, 0.0, emptyList())
        }

        val schedule = mutableListOf<SavingsScheduleItem>()
        var totalInterest = 0.0
        val maturityDate = DateTimeUtils.getPeriodDate(startDateStr, termMonths)

        when (paymentType) {
            SavingsPaymentType.END_OF_TERM -> {
                val step = if (periodMonths > 0) min(periodMonths, termMonths) else termMonths
                if (step >= termMonths) {
                    // Kỳ hạn bằng hoặc lớn hơn thời hạn: 1 kỳ duy nhất khi đáo hạn
                    totalInterest = depositAmount * (annualInterestRate / 100.0) * (termMonths / 12.0)
                    schedule.add(
                        SavingsScheduleItem(
                            period = 1,
                            payoutDate = maturityDate,
                            interestAmount = round(totalInterest),
                            principalAmount = round(depositAmount),
                            balanceAfter = round(depositAmount + totalInterest)
                        )
                    )
                } else {
                    // Tách biệt Thời hạn & Kỳ hạn: Lãi cuối mỗi kỳ hạn
                    var currentBalance = depositAmount
                    var currentM = 0
                    var periodIdx = 1

                    while (currentM < termMonths) {
                        val nextM = min(currentM + step, termMonths)
                        val monthsInPeriod = nextM - currentM
                        val isLast = nextM == termMonths

                        val periodInterest = if (isAutoRenewCompounding) {
                            currentBalance * (annualInterestRate / 100.0) * (monthsInPeriod / 12.0)
                        } else {
                            depositAmount * (annualInterestRate / 100.0) * (monthsInPeriod / 12.0)
                        }

                        totalInterest += periodInterest
                        val payoutDate = DateTimeUtils.getPeriodDate(startDateStr, nextM)

                        if (isAutoRenewCompounding) {
                            currentBalance += periodInterest
                            schedule.add(
                                SavingsScheduleItem(
                                    period = periodIdx++,
                                    payoutDate = payoutDate,
                                    interestAmount = round(periodInterest),
                                    principalAmount = if (isLast) round(currentBalance) else 0.0,
                                    balanceAfter = round(currentBalance)
                                )
                            )
                        } else {
                            schedule.add(
                                SavingsScheduleItem(
                                    period = periodIdx++,
                                    payoutDate = payoutDate,
                                    interestAmount = round(periodInterest),
                                    principalAmount = if (isLast) round(depositAmount) else 0.0,
                                    balanceAfter = if (isLast) 0.0 else round(depositAmount)
                                )
                            )
                        }
                        currentM = nextM
                    }
                }
            }

            SavingsPaymentType.MONTHLY -> {
                // Nhận lãi đều đặn mỗi tháng, gốc nhận cuối kỳ
                val monthlyInterest = depositAmount * ((annualInterestRate / 100.0) / 12.0)
                for (month in 1..termMonths) {
                    totalInterest += monthlyInterest
                    val payoutDate = DateTimeUtils.getPeriodDate(startDateStr, month)
                    val isLast = month == termMonths

                    schedule.add(
                        SavingsScheduleItem(
                            period = month,
                            payoutDate = payoutDate,
                            interestAmount = round(monthlyInterest),
                            principalAmount = if (isLast) round(depositAmount) else 0.0,
                            balanceAfter = if (isLast) 0.0 else round(depositAmount)
                        )
                    )
                }
            }

            SavingsPaymentType.QUARTERLY -> {
                // Nhận lãi mỗi 3 tháng
                val quarterlyInterest = depositAmount * ((annualInterestRate / 100.0) / 4.0)
                val totalQuarters = (termMonths / 3).coerceAtLeast(1)

                for (q in 1..totalQuarters) {
                    totalInterest += quarterlyInterest
                    val payoutDate = DateTimeUtils.getPeriodDate(startDateStr, q * 3)
                    val isLast = q == totalQuarters

                    schedule.add(
                        SavingsScheduleItem(
                            period = q,
                            payoutDate = payoutDate,
                            interestAmount = round(quarterlyInterest),
                            principalAmount = if (isLast) round(depositAmount) else 0.0,
                            balanceAfter = if (isLast) 0.0 else round(depositAmount)
                        )
                    )
                }
            }

            SavingsPaymentType.UPFRONT -> {
                // Nhận lãi ngay thời điểm gửi
                totalInterest = depositAmount * (annualInterestRate / 100.0) * (termMonths / 12.0)
                schedule.add(
                    SavingsScheduleItem(
                        period = 1,
                        payoutDate = startDateStr,
                        interestAmount = round(totalInterest),
                        principalAmount = 0.0,
                        balanceAfter = round(depositAmount)
                    )
                )
                schedule.add(
                    SavingsScheduleItem(
                        period = 2,
                        payoutDate = maturityDate,
                        interestAmount = 0.0,
                        principalAmount = round(depositAmount),
                        balanceAfter = 0.0
                    )
                )
            }
        }

        val totalMaturity = depositAmount + totalInterest
        val effectiveRate = if (depositAmount > 0 && termMonths > 0) {
            (totalInterest / depositAmount) * (12.0 / termMonths) * 100.0
        } else {
            annualInterestRate
        }

        return SavingsCalculationResult(
            principalAmount = depositAmount,
            totalInterest = round(totalInterest),
            totalMaturityAmount = round(totalMaturity),
            effectiveAnnualRate = effectiveRate,
            schedule = schedule
        )
    }

    /**
     * Tạo nội dung tin nhắn tư vấn gửi tiết kiệm gửi Zalo/SMS
     */
    fun generateConsultationText(
        customerName: String,
        depositAmount: Double,
        termMonths: Int,
        interestRate: Double,
        paymentType: SavingsPaymentType,
        isAutoRenewCompounding: Boolean,
        result: SavingsCalculationResult,
        startDateStr: String = DateTimeUtils.todayString(),
        periodMonths: Int = termMonths
    ): String {
        val name = if (customerName.isNotBlank()) customerName else "Quý khách"
        val sb = StringBuilder()
        sb.appendLine("🏦 BẢNG DỰ TÍNH TIỀN GỬI TIẾT KIỆM - TƯ VẤN NGÂN HÀNG")
        sb.appendLine("Kính gửi: Anh/Chị $name")
        sb.appendLine("--------------------------------------")
        sb.appendLine("• Số tiền gửi: ${CurrencyUtils.formatVnd(depositAmount)}")
        sb.appendLine("  (${CurrencyUtils.amountToWords(depositAmount.toLong())})")
        sb.appendLine("• Thời hạn gửi: $termMonths tháng")
        if (periodMonths != termMonths) {
            sb.appendLine("• Kỳ hạn: $periodMonths tháng")
        }
        if (startDateStr.isNotBlank()) {
            sb.appendLine("• Ngày gửi tiền: $startDateStr")
            sb.appendLine("• Ngày đáo hạn dự kiến: ${DateTimeUtils.getPeriodDate(startDateStr, termMonths)}")
        }
        sb.appendLine("• Lãi suất huy động: $interestRate%/năm")
        sb.appendLine("• Hình thức trả lãi: ${paymentType.title}")
        if (paymentType == SavingsPaymentType.END_OF_TERM && isAutoRenewCompounding) {
            sb.appendLine("  (Chế độ lãi kép: Tự động quay vòng nhập gốc)")
        }
        sb.appendLine("--------------------------------------")
        sb.appendLine("💰 DÒNG TIỀN LỢI NHUẬN DỰ KIẾN:")
        sb.appendLine("• Tổng tiền lãi thực nhận: ${CurrencyUtils.formatVnd(result.totalInterest)}")
        sb.appendLine("• Tổng tiền nhận khi tất toán: ${CurrencyUtils.formatVnd(result.totalMaturityAmount)}")
        if (paymentType == SavingsPaymentType.MONTHLY) {
            val monthly = if (termMonths > 0) result.totalInterest / termMonths else 0.0
            sb.appendLine("• Tiền lãi nhận định kỳ mỗi tháng: ${CurrencyUtils.formatVnd(monthly)}")
        } else if (paymentType == SavingsPaymentType.QUARTERLY) {
            val quarterly = result.schedule.firstOrNull()?.interestAmount ?: 0.0
            sb.appendLine("• Tiền lãi nhận định kỳ mỗi quý: ${CurrencyUtils.formatVnd(quarterly)}")
        }
        sb.appendLine("--------------------------------------")
        sb.appendLine("🛡️ Tiền gửi của Anh/Chị được bảo đảm an toàn 100% theo quy định của Ngân hàng Nhà nước.")
        sb.appendLine("Em luôn sẵn sàng hỗ trợ mở sổ tại quầy hoặc hướng dẫn gửi online với lãi suất tốt nhất!")
        return sb.toString()
    }
}
