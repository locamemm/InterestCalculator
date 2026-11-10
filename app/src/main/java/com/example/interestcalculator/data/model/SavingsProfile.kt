package com.example.interestcalculator.data.model

data class SavingsScheduleItem(
    val period: Int,
    val payoutDate: String,
    val interestAmount: Double,
    val principalAmount: Double,
    val balanceAfter: Double
)

data class SavingsCalculationResult(
    val principalAmount: Double,
    val totalInterest: Double,
    val totalMaturityAmount: Double,
    val effectiveAnnualRate: Double,
    val schedule: List<SavingsScheduleItem>
)

enum class SavingsPaymentType(val title: String, val description: String) {
    END_OF_TERM("Lãi cuối kỳ", "Nhận toàn bộ tiền lãi và gốc khi đáo hạn sổ"),
    MONTHLY("Lãi trả hàng tháng", "Nhận tiền lãi đều đặn mỗi tháng"),
    QUARTERLY("Lãi trả hàng quý", "Nhận tiền lãi đều đặn mỗi 3 tháng"),
    UPFRONT("Lãi trả đầu kỳ", "Nhận tiền lãi ngay thời điểm mở sổ")
}

enum class SavingsStatus(val title: String, val colorHex: Long) {
    ACTIVE("Đang gửi", 0xFF2E7D32),       // Green
    EXPIRING_SOON("Sắp đáo hạn", 0xFFE65100), // Orange / Amber
    SETTLED("Đã tất toán", 0xFF616161)     // Grey
}

data class SavingsProfile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val customerName: String,
    val customerPhone: String = "",
    val depositAmount: Double,
    val termMonths: Int,
    val periodMonths: Int = termMonths,
    val interestRate: Double,
    val interestPaymentType: SavingsPaymentType = SavingsPaymentType.END_OF_TERM,
    val isAutoRenewCompounding: Boolean = false,
    val depositDate: String = "",
    val maturityDate: String = "",
    val status: SavingsStatus = SavingsStatus.ACTIVE,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
