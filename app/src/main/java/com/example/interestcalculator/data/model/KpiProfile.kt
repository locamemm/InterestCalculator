package com.example.interestcalculator.data.model

data class KpiProfile(
    val staffName: String = "Chuyên viên Tín dụng & Huy động",
    val bankName: String = "Ngân hàng Thương mại",
    val loanDisbursementTarget: Double = 5_000_000_000.0, // 5 tỷ
    val savingsMobilizationTarget: Double = 10_000_000_000.0 // 10 tỷ
)
