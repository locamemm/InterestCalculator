package com.example.interestcalculator.data.model

import com.example.interestcalculator.util.DateTimeUtils
import java.util.UUID

data class SavedLoanParams(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val customerName: String = "",
    val principalAmount: Double = 1_000_000_000.0,
    val termMonths: Int = 120,
    val repaymentPeriodMonths: Int = 1,
    val baseRate: Double = 9.5,
    val isBaseRateMonthly: Boolean = false,
    val startDateStr: String = DateTimeUtils.todayString(),
    val hasPreferential: Boolean = true,
    val preferentialMonths: Int = 12,
    val preferentialRate: Double = 6.8,
    val hasGracePeriod: Boolean = false,
    val gracePeriodMonths: Int = 6,
    val selectedMethod: LoanMethod = LoanMethod.REDUCING_BALANCE,
    val savedAt: Long = System.currentTimeMillis()
)

data class SavedSavingsParams(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val customerName: String = "",
    val depositAmount: Double = 500_000_000.0,
    val termMonths: Int = 12,
    val periodMonths: Int = 6,
    val interestRate: Double = 5.8,
    val startDateStr: String = DateTimeUtils.todayString(),
    val paymentType: SavingsPaymentType = SavingsPaymentType.END_OF_TERM,
    val isCompounding: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
