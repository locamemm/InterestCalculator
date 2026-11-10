package com.example.interestcalculator.domain

import com.example.interestcalculator.data.model.DebtItemPeriodPayment
import com.example.interestcalculator.data.model.DebtPeriodBreakdown
import com.example.interestcalculator.data.model.DebtScheduleResult
import com.example.interestcalculator.data.model.DebtSourceItem
import com.example.interestcalculator.data.model.DebtSourceType
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

object DebtCalculator {

    fun calculate(
        items: List<DebtSourceItem>,
        startDateStr: String = DateTimeUtils.todayString(),
        bankRate: Double = 10.5,
        tctdRate: Double = 28.0,
        method: LoanMethod = LoanMethod.REDUCING_BALANCE
    ): DebtScheduleResult {
        val validItems = items.filter { it.currentDebt > 0 }
        if (validItems.isEmpty()) {
            return DebtScheduleResult(
                totalInitialDebt = 0.0,
                bankInitialDebt = 0.0,
                tctdInitialDebt = 0.0,
                totalInterest = 0.0,
                bankTotalInterest = 0.0,
                tctdTotalInterest = 0.0,
                totalPayment = 0.0,
                firstMonthPayment = 0.0,
                maxMonthPayment = 0.0,
                weightedAverageRate = 0.0,
                totalMonths = 0,
                schedule = emptyList()
            )
        }

        // Thông số của từng khoản nợ
        class ItemTracker(
            val item: DebtSourceItem,
            val effectiveRate: Double,
            val termMonths: Int,
            var currentBalance: Double
        )

        val trackers = validItems.map { item ->
            val rate = item.customRate ?: if (item.sourceType == DebtSourceType.BANK) bankRate else tctdRate
            val targetMaturity = if (item.maturityDate.isNotBlank()) item.maturityDate else DateTimeUtils.addMonths(startDateStr, 12)
            val months = max(1, DateTimeUtils.monthsBetween(startDateStr, targetMaturity))
            ItemTracker(
                item = item,
                effectiveRate = rate,
                termMonths = months,
                currentBalance = item.currentDebt
            )
        }

        val totalInitialDebt = validItems.sumOf { it.currentDebt }
        val bankInitialDebt = validItems.filter { it.sourceType == DebtSourceType.BANK }.sumOf { it.currentDebt }
        val tctdInitialDebt = validItems.filter { it.sourceType == DebtSourceType.CREDIT_INSTITUTION }.sumOf { it.currentDebt }

        val weightedRate = if (totalInitialDebt > 0) {
            trackers.sumOf { it.item.currentDebt * it.effectiveRate } / totalInitialDebt
        } else {
            0.0
        }

        val maxMonths = trackers.maxOfOrNull { it.termMonths } ?: 1
        val schedule = mutableListOf<DebtPeriodBreakdown>()

        var totalInterestAccumulated = 0.0
        var bankInterestAccumulated = 0.0
        var tctdInterestAccumulated = 0.0

        for (m in 1..maxMonths) {
            val paymentDate = DateTimeUtils.getPeriodDate(startDateStr, m)

            var periodPrincipal = 0.0
            var periodInterest = 0.0
            var periodBankPrincipal = 0.0
            var periodBankInterest = 0.0
            var periodTctdPrincipal = 0.0
            var periodTctdInterest = 0.0

            val itemPayments = mutableListOf<DebtItemPeriodPayment>()

            for (tracker in trackers) {
                if (m > tracker.termMonths || tracker.currentBalance <= 0.0) {
                    continue
                }

                val monthlyRate = (tracker.effectiveRate / 100.0) / 12.0
                val remainingPeriods = tracker.termMonths - m + 1
                val isLastMonth = m == tracker.termMonths

                val interestPayment: Double
                val principalPayment: Double

                when (method) {
                    LoanMethod.END_OF_TERM -> {
                        interestPayment = tracker.currentBalance * monthlyRate
                        principalPayment = if (isLastMonth) tracker.currentBalance else 0.0
                    }
                    LoanMethod.FLAT_RATE -> {
                        interestPayment = tracker.item.currentDebt * monthlyRate
                        val basePrincipal = tracker.item.currentDebt / tracker.termMonths
                        principalPayment = if (isLastMonth) tracker.currentBalance else min(basePrincipal, tracker.currentBalance)
                    }
                    LoanMethod.ANNUITY -> {
                        val pmt = if (monthlyRate > 0) {
                            val factor = (1 + monthlyRate).pow(remainingPeriods.toDouble())
                            tracker.currentBalance * (monthlyRate * factor) / (factor - 1)
                        } else {
                            tracker.currentBalance / remainingPeriods
                        }
                        val iVal = tracker.currentBalance * monthlyRate
                        val pVal = if (isLastMonth) tracker.currentBalance else max(0.0, min(pmt - iVal, tracker.currentBalance))
                        interestPayment = iVal
                        principalPayment = pVal
                    }
                    LoanMethod.REDUCING_BALANCE -> {
                        val iVal = tracker.currentBalance * monthlyRate
                        val basePrincipal = tracker.item.currentDebt / tracker.termMonths
                        val pVal = if (isLastMonth) tracker.currentBalance else min(basePrincipal, tracker.currentBalance)
                        interestPayment = iVal
                        principalPayment = pVal
                    }
                }

                val newBal = max(0.0, tracker.currentBalance - principalPayment)
                tracker.currentBalance = newBal

                periodPrincipal += principalPayment
                periodInterest += interestPayment

                if (tracker.item.sourceType == DebtSourceType.BANK) {
                    periodBankPrincipal += principalPayment
                    periodBankInterest += interestPayment
                } else {
                    periodTctdPrincipal += principalPayment
                    periodTctdInterest += interestPayment
                }

                itemPayments.add(
                    DebtItemPeriodPayment(
                        itemId = tracker.item.id,
                        itemName = tracker.item.name.ifBlank { tracker.item.sourceType.title },
                        sourceType = tracker.item.sourceType,
                        principalPayment = round(principalPayment),
                        interestPayment = round(interestPayment),
                        totalPayment = round(principalPayment + interestPayment),
                        remainingBalance = round(newBal)
                    )
                )
            }

            val totalRemaining = trackers.sumOf { it.currentBalance }
            val periodTotal = periodPrincipal + periodInterest

            totalInterestAccumulated += periodInterest
            bankInterestAccumulated += periodBankInterest
            tctdInterestAccumulated += periodTctdInterest

            schedule.add(
                DebtPeriodBreakdown(
                    period = m,
                    paymentDate = paymentDate,
                    principalPayment = round(periodPrincipal),
                    interestPayment = round(periodInterest),
                    totalPayment = round(periodTotal),
                    remainingBalance = round(totalRemaining),
                    bankPrincipalPayment = round(periodBankPrincipal),
                    bankInterestPayment = round(periodBankInterest),
                    tctdPrincipalPayment = round(periodTctdPrincipal),
                    tctdInterestPayment = round(periodTctdInterest),
                    itemBreakdowns = itemPayments
                )
            )
        }

        val firstMonth = schedule.firstOrNull()?.totalPayment ?: 0.0
        val maxMonth = schedule.maxOfOrNull { it.totalPayment } ?: 0.0

        return DebtScheduleResult(
            totalInitialDebt = round(totalInitialDebt),
            bankInitialDebt = round(bankInitialDebt),
            tctdInitialDebt = round(tctdInitialDebt),
            totalInterest = round(totalInterestAccumulated),
            bankTotalInterest = round(bankInterestAccumulated),
            tctdTotalInterest = round(tctdInterestAccumulated),
            totalPayment = round(totalInitialDebt + totalInterestAccumulated),
            firstMonthPayment = firstMonth,
            maxMonthPayment = maxMonth,
            weightedAverageRate = round(weightedRate * 100) / 100.0,
            totalMonths = maxMonths,
            schedule = schedule
        )
    }

    /**
     * Tạo văn bản tư vấn tổng hợp nợ đa nguồn để gửi Zalo/SMS cho khách hàng
     */
    fun generateDebtConsultationText(
        customerName: String,
        startDateStr: String,
        bankRate: Double,
        tctdRate: Double,
        items: List<DebtSourceItem>,
        result: DebtScheduleResult
    ): String {
        val name = if (customerName.isNotBlank()) customerName else "Quý khách"
        val sb = StringBuilder()
        sb.appendLine("📑 BẢNG TỔNG HỢP DƯ NỢ & DÒNG TIỀN TRẢ NỢ HÀNG THÁNG")
        sb.appendLine("Kính gửi: Anh/Chị $name")
        sb.appendLine("• Mốc thời gian tổng hợp: $startDateStr")
        sb.appendLine("--------------------------------------")
        sb.appendLine("📊 DANH SÁCH CÁC KHOẢN DƯ NỢ HIỆN TẠI:")

        val bankItems = items.filter { it.sourceType == DebtSourceType.BANK && it.currentDebt > 0 }
        val tctdItems = items.filter { it.sourceType == DebtSourceType.CREDIT_INSTITUTION && it.currentDebt > 0 }

        if (bankItems.isNotEmpty()) {
            sb.appendLine("1. KHỐI NGÂN HÀNG (Lãi suất áp dụng: $bankRate%/năm):")
            bankItems.forEachIndexed { idx, item ->
                val rate = item.customRate ?: bankRate
                val customStr = if (item.customRate != null) " (Lãi riêng: $rate%)" else ""
                val matStr = if (item.maturityDate.isNotBlank()) " - Đáo hạn: ${item.maturityDate}" else ""
                sb.appendLine("   ${idx + 1}. ${item.name.ifBlank { "Ngân hàng" }}: ${CurrencyUtils.formatVnd(item.currentDebt)}$matStr$customStr")
            }
            sb.appendLine("   => Tổng dư nợ Ngân hàng: ${CurrencyUtils.formatVnd(result.bankInitialDebt)}")
        }

        if (tctdItems.isNotEmpty()) {
            sb.appendLine("2. KHỐI TỔ CHỨC TÍN DỤNG (Lãi suất áp dụng: $tctdRate%/năm):")
            tctdItems.forEachIndexed { idx, item ->
                val rate = item.customRate ?: tctdRate
                val customStr = if (item.customRate != null) " (Lãi riêng: $rate%)" else ""
                val matStr = if (item.maturityDate.isNotBlank()) " - Đáo hạn: ${item.maturityDate}" else ""
                sb.appendLine("   ${idx + 1}. ${item.name.ifBlank { "Tổ chức tín dụng" }}: ${CurrencyUtils.formatVnd(item.currentDebt)}$matStr$customStr")
            }
            sb.appendLine("   => Tổng dư nợ TCTD: ${CurrencyUtils.formatVnd(result.tctdInitialDebt)}")
        }

        sb.appendLine("--------------------------------------")
        sb.appendLine("📈 TỔNG HỢP DANH MỤC:")
        sb.appendLine("• Tổng dư nợ tất cả các nguồn: ${CurrencyUtils.formatVnd(result.totalInitialDebt)}")
        sb.appendLine("  (${CurrencyUtils.amountToWords(result.totalInitialDebt.toLong())})")
        sb.appendLine("• Lãi suất bình quân gia quyền: ${result.weightedAverageRate}%/năm (~ ${String.format(java.util.Locale.US, "%.2f", result.weightedAverageRate / 12.0)}%/tháng)")
        sb.appendLine("• Thời gian trả nợ đến khoản đáo hạn cuối: ${result.totalMonths} tháng")
        sb.appendLine("--------------------------------------")
        sb.appendLine("💵 DỰ TOÁN NGHĨA VỤ THANH TOÁN:")
        sb.appendLine("• Số tiền trả tháng đầu tiên: ${CurrencyUtils.formatVnd(result.firstMonthPayment)}")
        sb.appendLine("• Số tiền trả cao nhất/tháng: ${CurrencyUtils.formatVnd(result.maxMonthPayment)}")
        sb.appendLine("• Tổng tiền lãi dự kiến cả chu kỳ: ${CurrencyUtils.formatVnd(result.totalInterest)}")
        sb.appendLine("  - Lãi Ngân hàng: ${CurrencyUtils.formatVnd(result.bankTotalInterest)}")
        sb.appendLine("  - Lãi Tổ chức tín dụng: ${CurrencyUtils.formatVnd(result.tctdTotalInterest)}")
        sb.appendLine("• Tổng cộng tiền gốc & lãi cần trả: ${CurrencyUtils.formatVnd(result.totalPayment)}")
        sb.appendLine("--------------------------------------")
        sb.appendLine("💡 Khuyến nghị tài chính:")
        if (result.tctdInitialDebt > 0) {
            sb.appendLine("👉 Khoản nợ TCTD có lãi suất cao (${tctdRate}%/năm). Em khuyên Anh/Chị nên ưu tiên tất toán sớm hoặc xem xét phương án vay Ngân hàng để gom nợ, giúp giảm áp lực tài chính hàng tháng!")
        } else {
            sb.appendLine("👉 Danh mục nợ hiện tại được quản lý tốt với lãi suất ưu đãi từ Ngân hàng.")
        }
        return sb.toString()
    }
}
