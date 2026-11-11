//
//  SavingsProfile.swift
//  InterestCalculator
//

import Foundation
import SwiftUI

struct SavingsScheduleItem: Identifiable, Codable, Equatable {
    var id: Int { period }
    let period: Int
    let payoutDate: String
    let interestAmount: Double
    let principalAmount: Double
    let balanceAfter: Double
}

struct SavingsCalculationResult: Codable, Equatable {
    let principalAmount: Double
    let totalInterest: Double
    let totalMaturityAmount: Double
    let effectiveAnnualRate: Double
    let schedule: [SavingsScheduleItem]
}

enum SavingsPaymentType: String, CaseIterable, Codable {
    case endOfTerm = "END_OF_TERM"
    case monthly = "MONTHLY"
    case quarterly = "QUARTERLY"
    case upfront = "UPFRONT"
    
    var title: String {
        switch self {
        case .endOfTerm: return "Lãi cuối kỳ"
        case .monthly: return "Lãi trả hàng tháng"
        case .quarterly: return "Lãi trả hàng quý"
        case .upfront: return "Lãi trả đầu kỳ"
        }
    }
    
    var shortTitle: String {
        switch self {
        case .endOfTerm: return "Cuối kỳ"
        case .monthly: return "Hàng tháng"
        case .quarterly: return "Hàng quý"
        case .upfront: return "Đầu kỳ"
        }
    }
    
    var description: String {
        switch self {
        case .endOfTerm: return "Nhận toàn bộ tiền lãi và gốc khi đáo hạn sổ"
        case .monthly: return "Nhận tiền lãi đều đặn mỗi tháng"
        case .quarterly: return "Nhận tiền lãi đều đặn mỗi 3 tháng"
        case .upfront: return "Nhận tiền lãi ngay thời điểm mở sổ"
        }
    }
}

enum SavingsStatus: String, CaseIterable, Codable {
    case active = "ACTIVE"
    case expiringSoon = "EXPIRING_SOON"
    case settled = "SETTLED"
    
    var title: String {
        switch self {
        case .active: return "Đang gửi"
        case .expiringSoon: return "Sắp đáo hạn"
        case .settled: return "Đã tất toán"
        }
    }
    
    var color: Color {
        switch self {
        case .active: return Color(red: 0x2E/255.0, green: 0x7D/255.0, blue: 0x32/255.0)
        case .expiringSoon: return Color(red: 0xE6/255.0, green: 0x51/255.0, blue: 0x00/255.0)
        case .settled: return Color(red: 0x61/255.0, green: 0x61/255.0, blue: 0x61/255.0)
        }
    }
}

struct SavingsProfile: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var customerName: String
    var customerPhone: String = ""
    var depositAmount: Double
    var termMonths: Int
    var periodMonths: Int
    var interestRate: Double
    var interestPaymentType: SavingsPaymentType = .endOfTerm
    var isAutoRenewCompounding: Bool = false
    var depositDate: String = ""
    var maturityDate: String = ""
    var status: SavingsStatus = .active
    var note: String = ""
    var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    init(
        id: String = UUID().uuidString,
        customerName: String,
        customerPhone: String = "",
        depositAmount: Double,
        termMonths: Int,
        periodMonths: Int? = nil,
        interestRate: Double,
        interestPaymentType: SavingsPaymentType = .endOfTerm,
        isAutoRenewCompounding: Bool = false,
        depositDate: String = "",
        maturityDate: String = "",
        status: SavingsStatus = .active,
        note: String = "",
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.customerName = customerName
        self.customerPhone = customerPhone
        self.depositAmount = depositAmount
        self.termMonths = termMonths
        self.periodMonths = periodMonths ?? termMonths
        self.interestRate = interestRate
        self.interestPaymentType = interestPaymentType
        self.isAutoRenewCompounding = isAutoRenewCompounding
        self.depositDate = depositDate
        self.maturityDate = maturityDate
        self.status = status
        self.note = note
        self.createdAt = createdAt
    }
}

