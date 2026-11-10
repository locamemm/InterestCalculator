package com.example.interestcalculator.data.model

data class RepaymentScheduleItem(
    val period: Int,
    val paymentDate: String,
    val principalPayment: Double,
    val interestPayment: Double,
    val totalPayment: Double,
    val remainingBalance: Double
)

data class LoanCalculationResult(
    val totalPrincipal: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val firstMonthPayment: Double,
    val lastMonthPayment: Double,
    val maxMonthPayment: Double,
    val schedule: List<RepaymentScheduleItem>
)

enum class LoanMethod(val title: String, val description: String) {
    REDUCING_BALANCE("Dư nợ giảm dần", "Gốc chia đều, lãi tính trên dư nợ thực tế (Chuẩn NH)"),
    ANNUITY("Trả góp đều (Niên kim)", "Tổng số tiền gốc + lãi cố định hàng tháng"),
    FLAT_RATE("Lãi phẳng (Dư nợ gốc)", "Lãi tính cố định theo số tiền vay ban đầu"),
    END_OF_TERM("Lãi cuối kỳ", "Gốc và lãi trả toàn bộ một lần khi đáo hạn")
}

enum class LoanStatus(val title: String, val colorHex: Long) {
    CONSULTING("Đang tư vấn", 0xFF1976D2), // Blue
    APPROVED("Đã duyệt", 0xFF7B1FA2),     // Purple
    DISBURSED("Đã giải ngân", 0xFF2E7D32), // Green
    SETTLED("Đã tất toán", 0xFF616161)     // Grey
}

data class LoanProfile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val customerName: String,
    val customerPhone: String = "",
    val loanPurpose: String = "Mua nhà đất",
    val principalAmount: Double,
    val termMonths: Int,
    val repaymentPeriodMonths: Int = 1,
    val interestRate: Double,
    val hasPreferential: Boolean = false,
    val preferentialMonths: Int = 12,
    val preferentialRate: Double = interestRate,
    val gracePeriodMonths: Int = 0,
    val calculationMethod: LoanMethod = LoanMethod.REDUCING_BALANCE,
    val disbursementDate: String = "",
    val status: LoanStatus = LoanStatus.CONSULTING,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
