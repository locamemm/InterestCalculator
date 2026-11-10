//
//  KpiProfile.swift
//  InterestCalculator
//

import Foundation

struct KpiProfile: Codable, Equatable {
    var staffName: String = "Chuyên viên Tín dụng & Huy động"
    var bankName: String = "Ngân hàng Thương mại"
    var loanDisbursementTarget: Double = 5_000_000_000.0 // 5 tỷ
    var savingsMobilizationTarget: Double = 10_000_000_000.0 // 10 tỷ
}
