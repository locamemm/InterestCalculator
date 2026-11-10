package com.example.interestcalculator

import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.SavingsPaymentType
import com.example.interestcalculator.domain.LoanCalculator
import com.example.interestcalculator.domain.SavingsCalculator
import com.example.interestcalculator.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {

    @Test
    fun testLoanReducingBalanceStandard() {
        // Khoản vay 120.000.000 ₫ trong 12 tháng, lãi suất 12%/năm
        // Gốc mỗi tháng = 10.000.000 ₫
        // Tháng 1: Dư nợ 120tr, Lãi = 120tr * 1% = 1.200.000 ₫ -> Tổng trả tháng 1 = 11.200.000 ₫
        // Tháng 12: Dư nợ 10tr, Lãi = 10tr * 1% = 100.000 ₫ -> Tổng trả tháng 12 = 10.100.000 ₫
        val result = LoanCalculator.calculate(
            principalAmount = 120_000_000.0,
            termMonths = 12,
            baseInterestRate = 12.0,
            method = LoanMethod.REDUCING_BALANCE,
            startDateStr = "01/01/2026"
        )

        assertEquals(12, result.schedule.size)
        assertEquals(120_000_000.0, result.totalPrincipal, 0.01)
        assertEquals(11_200_000.0, result.firstMonthPayment, 1.0)
        assertEquals(10_100_000.0, result.lastMonthPayment, 1.0)
        // Tổng lãi của 120tr qua 12 tháng: 120 * (12+1)/2 * 10tr * 0.01 = 7.800.000 ₫
        assertEquals(7_800_000.0, result.totalInterest, 10.0)
        assertEquals(0.0, result.schedule.last().remainingBalance, 0.01)
        assertEquals("01/02/2026", result.schedule.first().paymentDate)
        assertEquals("01/01/2027", result.schedule.last().paymentDate)
    }

    @Test
    fun testStartDateScheduleCalculation() {
        val startDate = "15/03/2026"
        val loanResult = LoanCalculator.calculate(
            principalAmount = 60_000_000.0,
            termMonths = 6,
            baseInterestRate = 12.0,
            startDateStr = startDate
        )
        assertEquals("15/04/2026", loanResult.schedule[0].paymentDate)
        assertEquals("15/09/2026", loanResult.schedule.last().paymentDate)

        val savingsResult = SavingsCalculator.calculate(
            depositAmount = 50_000_000.0,
            termMonths = 6,
            annualInterestRate = 6.0,
            startDateStr = startDate
        )
        assertEquals("15/09/2026", savingsResult.schedule.first().payoutDate)
    }

    @Test
    fun testLoanWithGracePeriodAndPreferentialRate() {
        // Vay 100tr, 12 tháng. 3 tháng đầu ân hạn gốc (chỉ trả lãi), lãi ưu đãi 6%/năm.
        // Tháng 4-12 (9 tháng) trả gốc đều 100tr / 9, lãi 10%/năm.
        val result = LoanCalculator.calculate(
            principalAmount = 100_000_000.0,
            termMonths = 12,
            baseInterestRate = 10.0,
            hasPreferential = true,
            preferentialMonths = 3,
            preferentialRate = 6.0,
            gracePeriodMonths = 3,
            method = LoanMethod.REDUCING_BALANCE
        )

        // 3 tháng đầu gốc trả = 0
        assertEquals(0.0, result.schedule[0].principalPayment, 0.01)
        assertEquals(0.0, result.schedule[1].principalPayment, 0.01)
        assertEquals(0.0, result.schedule[2].principalPayment, 0.01)

        // Tháng 1: Lãi = 100tr * (6% / 12) = 500.000 ₫
        assertEquals(500_000.0, result.schedule[0].interestPayment, 1.0)

        // Cuối kỳ dư nợ = 0
        assertEquals(0.0, result.schedule.last().remainingBalance, 0.01)
    }

    @Test
    fun testLoanAnnuityFixedInstallment() {
        // Trả góp đều (Niên kim cố định)
        val result = LoanCalculator.calculate(
            principalAmount = 100_000_000.0,
            termMonths = 12,
            baseInterestRate = 12.0,
            method = LoanMethod.ANNUITY
        )

        assertEquals(12, result.schedule.size)
        // Mỗi tháng số tiền trả xấp xỉ bằng nhau (~8.884.879 ₫)
        val pmt = result.firstMonthPayment
        assertTrue(pmt > 8_800_000.0 && pmt < 9_000_000.0)
        assertEquals(0.0, result.schedule.last().remainingBalance, 0.01)
    }

    @Test
    fun testSavingsEndOfTerm() {
        // Gửi tiết kiệm 100.000.000 ₫, kỳ hạn 6 tháng, lãi suất 6%/năm cuối kỳ
        // Tiền lãi = 100tr * 6% * (6/12) = 3.000.000 ₫
        val result = SavingsCalculator.calculate(
            depositAmount = 100_000_000.0,
            termMonths = 6,
            annualInterestRate = 6.0,
            paymentType = SavingsPaymentType.END_OF_TERM,
            isAutoRenewCompounding = false
        )

        assertEquals(3_000_000.0, result.totalInterest, 1.0)
        assertEquals(103_000_000.0, result.totalMaturityAmount, 1.0)
    }

    @Test
    fun testSavingsMonthlyPayout() {
        // Gửi 120.000.000 ₫, kỳ hạn 12 tháng, lãi 6%/năm nhận hàng tháng
        // Mỗi tháng nhận 120tr * 0.5% = 600.000 ₫
        val result = SavingsCalculator.calculate(
            depositAmount = 120_000_000.0,
            termMonths = 12,
            annualInterestRate = 6.0,
            paymentType = SavingsPaymentType.MONTHLY
        )

        assertEquals(12, result.schedule.size)
        assertEquals(600_000.0, result.schedule[0].interestAmount, 1.0)
        assertEquals(7_200_000.0, result.totalInterest, 1.0)
    }

    @Test
    fun testCurrencyFormattingAndWords() {
        assertEquals("1.000.000 ₫", CurrencyUtils.formatVnd(1_000_000L))
        assertEquals("1.500 triệu", CurrencyUtils.formatCompactVnd(1_500_000_000.0))
        assertEquals("500 triệu", CurrencyUtils.formatCompactVnd(500_000_000.0))

        val words1 = CurrencyUtils.amountToWords(1_000_000_000L)
        assertTrue(words1.contains("Một tỷ đồng"))

        val words2 = CurrencyUtils.amountToWords(500_000_000L)
        assertTrue(words2.contains("Năm trăm triệu đồng"))
    }

    @Test
    fun testScheduleCurrencyFormatting() {
        assertEquals("10 tr", CurrencyUtils.formatScheduleVnd(10_000_000.0))
        assertEquals("500 tr", CurrencyUtils.formatScheduleVnd(500_000_000.0))
        assertEquals("1.500 tr", CurrencyUtils.formatScheduleVnd(1_500_000_000.0))
        assertEquals("8,33 tr", CurrencyUtils.formatScheduleVnd(8_333_333.33))
        assertEquals("12,5 tr", CurrencyUtils.formatScheduleVnd(12_500_000.0))
        assertEquals("12,25 tr", CurrencyUtils.formatScheduleVnd(12_250_000.0))
        assertEquals("91,67 tr", CurrencyUtils.formatScheduleVnd(91_666_666.67))
        assertEquals("500k", CurrencyUtils.formatScheduleVnd(500_000.0))
        assertEquals("833,33k", CurrencyUtils.formatScheduleVnd(833_333.33))
        assertEquals("0 ₫", CurrencyUtils.formatScheduleVnd(0.0))
    }

    @Test
    fun testDateTimeUtilsFormatShortYearDate() {
        assertEquals("15/10/26", com.example.interestcalculator.util.DateTimeUtils.formatShortYearDate("15/10/2026"))
        assertEquals("01/01/27", com.example.interestcalculator.util.DateTimeUtils.formatShortYearDate("01/01/2027"))
        assertEquals("05/09/25", com.example.interestcalculator.util.DateTimeUtils.formatShortYearDate("5/9/2025"))
    }

    @Test
    fun testDuplicateCustomerNameDetection() {
        val existingNames = listOf("Nguyễn Văn A", "Trần Thị B", "Lê Văn C")
        val isDuplicate1 = existingNames.any { it.trim().equals("  nguyễn văn a  ".trim(), ignoreCase = true) }
        val isDuplicate2 = existingNames.any { it.trim().equals("Trần Thị B".trim(), ignoreCase = true) }
        val isDuplicate3 = existingNames.any { it.trim().equals("Phạm Văn D".trim(), ignoreCase = true) }

        assertTrue(isDuplicate1)
        assertTrue(isDuplicate2)
        org.junit.Assert.assertFalse(isDuplicate3)
    }

    @Test
    fun testLoanEndOfTermSinglePeriod() {
        // Vay 100.000.000 ₫, thời hạn 12 tháng, lãi suất 12%/năm, Lãi cuối kỳ
        val result = LoanCalculator.calculate(
            principalAmount = 100_000_000.0,
            termMonths = 12,
            baseInterestRate = 12.0,
            method = LoanMethod.END_OF_TERM,
            repaymentPeriodMonths = 12,
            startDateStr = "01/01/2026"
        )

        assertEquals(1, result.schedule.size)
        assertEquals(100_000_000.0, result.totalPrincipal, 0.01)
        assertEquals(12_000_000.0, result.totalInterest, 1.0)
        assertEquals(112_000_000.0, result.totalPayment, 1.0)
        assertEquals(100_000_000.0, result.schedule[0].principalPayment, 0.01)
        assertEquals(12_000_000.0, result.schedule[0].interestPayment, 1.0)
        assertEquals(0.0, result.schedule[0].remainingBalance, 0.01)
        assertEquals("01/01/2027", result.schedule[0].paymentDate)
    }

    @Test
    fun testSavingsEndOfTermSeparatedTermAndPeriod() {
        // Gửi 100.000.000 ₫, thời hạn 12 tháng, kỳ hạn 3 tháng (4 kỳ), lãi 6%/năm
        // 1. Không quay vòng (lãi đơn mỗi quý 1.5tr)
        val resultSimple = SavingsCalculator.calculate(
            depositAmount = 100_000_000.0,
            termMonths = 12,
            periodMonths = 3,
            annualInterestRate = 6.0,
            paymentType = SavingsPaymentType.END_OF_TERM,
            isAutoRenewCompounding = false,
            startDateStr = "01/01/2026"
        )
        assertEquals(4, resultSimple.schedule.size)
        assertEquals(1_500_000.0, resultSimple.schedule[0].interestAmount, 1.0)
        assertEquals(6_000_000.0, resultSimple.totalInterest, 1.0)
        assertEquals(106_000_000.0, resultSimple.totalMaturityAmount, 1.0)

        // 2. Có quay vòng nhập gốc (lãi kép mỗi quý)
        val resultCompounding = SavingsCalculator.calculate(
            depositAmount = 100_000_000.0,
            termMonths = 12,
            periodMonths = 3,
            annualInterestRate = 6.0,
            paymentType = SavingsPaymentType.END_OF_TERM,
            isAutoRenewCompounding = true,
            startDateStr = "01/01/2026"
        )
        assertEquals(4, resultCompounding.schedule.size)
        // Kỳ 1: 1.500.000, Kỳ 2: 101.5tr * 1.5% = 1.522.500, Kỳ 3: 1.545.338, Kỳ 4: 1.568.518 -> Tổng: ~6.136.355 ₫
        assertEquals(6_136_355.0, resultCompounding.totalInterest, 10.0)
        assertTrue(resultCompounding.totalInterest > resultSimple.totalInterest)
    }

    @Test
    fun testMonthlyInterestRateEquivalentCalculation() {
        // Kiểm tra tính toán tương đương giữa lãi suất 1%/tháng và 12%/năm
        val monthlyRate = 1.0
        val yearlyEquivalent = monthlyRate * 12.0
        assertEquals(12.0, yearlyEquivalent, 0.001)

        val resultFromYearly = LoanCalculator.calculate(
            principalAmount = 100_000_000.0,
            termMonths = 12,
            baseInterestRate = yearlyEquivalent,
            method = LoanMethod.REDUCING_BALANCE,
            startDateStr = "01/01/2026"
        )

        // Tháng 1: 100tr * 1%/tháng = 1.000.000 ₫ lãi
        assertEquals(1_000_000.0, resultFromYearly.schedule[0].interestPayment, 1.0)
        // 0.8%/tháng = 9.6%/năm
        val monthlyRate2 = 0.8
        val yearlyEquivalent2 = monthlyRate2 * 12.0
        assertEquals(9.6, yearlyEquivalent2, 0.001)
    }
}
