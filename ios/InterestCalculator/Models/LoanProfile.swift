//
//  LoanProfile.swift
//  InterestCalculator
//

import Foundation
import SwiftUI

struct RepaymentScheduleItem: Identifiable, Codable, Equatable {
    var id: Int { period }
    let period: Int
    let paymentDate: String
    let principalPayment: Double
    let interestPayment: Double
    let totalPayment: Double
    let remainingBalance: Double
}

struct LoanCalculationResult: Codable, Equatable {
    let totalPrincipal: Double
    let totalInterest: Double
    let totalPayment: Double
    let firstMonthPayment: Double
    let lastMonthPayment: Double
    let maxMonthPayment: Double
    let schedule: [RepaymentScheduleItem]
}

enum LoanMethod: String, CaseIterable, Codable {
    case reducingBalance = "REDUCING_BALANCE"
    case annuity = "ANNUITY"
    case flatRate = "FLAT_RATE"
    case endOfTerm = "END_OF_TERM"
    
    var title: String {
        switch self {
        case .reducingBalance: return "Dư nợ giảm dần"
        case .annuity: return "Trả góp đều (Niên kim)"
        case .flatRate: return "Lãi phẳng (Dư nợ gốc)"
        case .endOfTerm: return "Lãi cuối kỳ"
        }
    }
    
    var description: String {
        switch self {
        case .reducingBalance: return "Gốc chia đều, lãi tính trên dư nợ thực tế (Chuẩn NH)"
        case .annuity: return "Tổng số tiền gốc + lãi cố định hàng tháng"
        case .flatRate: return "Lãi tính cố định theo số tiền vay ban đầu"
        case .endOfTerm: return "Gốc và lãi trả toàn bộ một lần khi đáo hạn"
        }
    }
}

enum LoanStatus: String, CaseIterable, Codable {
    case consulting = "CONSULTING"
    case approved = "APPROVED"
    case disbursed = "DISBURSED"
    case settled = "SETTLED"
    
    var title: String {
        switch self {
        case .consulting: return "Đang tư vấn"
        case .approved: return "Đã duyệt"
        case .disbursed: return "Đã giải ngân"
        case .settled: return "Đã tất toán"
        }
    }
    
    var color: Color {
        switch self {
        case .consulting: return Color(red: 0x19/255.0, green: 0x76/255.0, blue: 0xD2/255.0)
        case .approved: return Color(red: 0x7B/255.0, green: 0x1F/255.0, blue: 0xA2/255.0)
        case .disbursed: return Color(red: 0x2E/255.0, green: 0x7D/255.0, blue: 0x32/255.0)
        case .settled: return Color(red: 0x61/255.0, green: 0x61/255.0, blue: 0x61/255.0)
        }
    }
}

struct LoanProfile: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var customerName: String
    var customerPhone: String = ""
    var loanPurpose: String = "Mua nhà đất"
    var principalAmount: Double
    var termMonths: Int
    var repaymentPeriodMonths: Int = 1
    var interestRate: Double
    var hasPreferential: Bool = false
    var preferentialMonths: Int = 12
    var preferentialRate: Double = 0.0
    var gracePeriodMonths: Int = 0
    var calculationMethod: LoanMethod = .reducingBalance
    var disbursementDate: String = ""
    var status: LoanStatus = .consulting
    var note: String = ""
    var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    init(
        id: String = UUID().uuidString,
        customerName: String,
        customerPhone: String = "",
        loanPurpose: String = "Mua nhà đất",
        principalAmount: Double,
        termMonths: Int,
        repaymentPeriodMonths: Int = 1,
        interestRate: Double,
        hasPreferential: Bool = false,
        preferentialMonths: Int = 12,
        preferentialRate: Double? = nil,
        gracePeriodMonths: Int = 0,
        calculationMethod: LoanMethod = .reducingBalance,
        disbursementDate: String = "",
        status: LoanStatus = .consulting,
        note: String = "",
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.customerName = customerName
        self.customerPhone = customerPhone
        self.loanPurpose = loanPurpose
        self.principalAmount = principalAmount
        self.termMonths = termMonths
        self.repaymentPeriodMonths = repaymentPeriodMonths
        self.interestRate = interestRate
        self.hasPreferential = hasPreferential
        self.preferentialMonths = preferentialMonths
        self.preferentialRate = preferentialRate ?? interestRate
        self.gracePeriodMonths = gracePeriodMonths
        self.calculationMethod = calculationMethod
        self.disbursementDate = disbursementDate
        self.status = status
        self.note = note
        self.createdAt = createdAt
    }
}

