package com.example.interestcalculator.data.model

import com.example.interestcalculator.util.DateTimeUtils
import java.util.UUID

enum class DebtSourceType(val title: String, val shortTitle: String, val colorHex: Long) {
    BANK("Ngân hàng", "NH", 0xFF1976D2), // Blue
    CREDIT_INSTITUTION("Tổ chức tín dụng", "TCTD", 0xFFE65100) // Deep Orange
}

data class DebtSourceItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val sourceType: DebtSourceType = DebtSourceType.BANK,
    val currentDebt: Double = 0.0,
    val maturityDate: String = "",
    val customRate: Double? = null // Ghi đè lãi suất riêng nếu cần (%/năm), null thì dùng lãi chung của loại
)

data class DebtItemPeriodPayment(
    val itemId: String,
    val itemName: String,
    val sourceType: DebtSourceType,
    val principalPayment: Double,
    val interestPayment: Double,
    val totalPayment: Double,
    val remainingBalance: Double
)

data class DebtPeriodBreakdown(
    val period: Int,
    val paymentDate: String,
    val principalPayment: Double,
    val interestPayment: Double,
    val totalPayment: Double,
    val remainingBalance: Double,
    val bankPrincipalPayment: Double,
    val bankInterestPayment: Double,
    val tctdPrincipalPayment: Double,
    val tctdInterestPayment: Double,
    val itemBreakdowns: List<DebtItemPeriodPayment> = emptyList()
)

data class DebtScheduleResult(
    val totalInitialDebt: Double,
    val bankInitialDebt: Double,
    val tctdInitialDebt: Double,
    val totalInterest: Double,
    val bankTotalInterest: Double,
    val tctdTotalInterest: Double,
    val totalPayment: Double,
    val firstMonthPayment: Double,
    val maxMonthPayment: Double,
    val weightedAverageRate: Double,
    val totalMonths: Int,
    val schedule: List<DebtPeriodBreakdown>
)

data class SavedDebtParams(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val customerName: String = "",
    val startDateStr: String = DateTimeUtils.todayString(),
    val bankRate: Double = 10.5,
    val isBankRateMonthly: Boolean = false,
    val tctdRate: Double = 28.0,
    val isTctdRateMonthly: Boolean = false,
    val calculationMethod: LoanMethod = LoanMethod.REDUCING_BALANCE,
    val items: List<DebtSourceItem> = emptyList(),
    val savedAt: Long = System.currentTimeMillis()
)
