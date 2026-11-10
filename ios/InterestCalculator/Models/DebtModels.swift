//
//  DebtModels.swift
//  InterestCalculator
//

import Foundation
import SwiftUI

enum DebtSourceType: String, CaseIterable, Codable {
    case bank = "BANK"
    case creditInstitution = "CREDIT_INSTITUTION"
    
    var title: String {
        switch self {
        case .bank: return "Ngân hàng"
        case .creditInstitution: return "Tổ chức tín dụng"
        }
    }
    
    var shortTitle: String {
        switch self {
        case .bank: return "NH"
        case .creditInstitution: return "TCTD"
        }
    }
    
    var color: Color {
        switch self {
        case .bank: return Color(red: 0x19/255.0, green: 0x76/255.0, blue: 0xD2/255.0)
        case .creditInstitution: return Color(red: 0xE6/255.0, green: 0x51/255.0, blue: 0x00/255.0)
        }
    }
}

struct DebtSourceItem: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var sourceType: DebtSourceType = .bank
    var currentDebt: Double = 0.0
    var maturityDate: String = ""
    var customRate: Double? = nil
}

struct DebtItemPeriodPayment: Identifiable, Codable, Equatable {
    var id: String { itemId }
    let itemId: String
    let itemName: String
    let sourceType: DebtSourceType
    let principalPayment: Double
    let interestPayment: Double
    let totalPayment: Double
    let remainingBalance: Double
}

struct DebtPeriodBreakdown: Identifiable, Codable, Equatable {
    var id: Int { period }
    let period: Int
    let paymentDate: String
    let principalPayment: Double
    let interestPayment: Double
    let totalPayment: Double
    let remainingBalance: Double
    let bankPrincipalPayment: Double
    let bankInterestPayment: Double
    let tctdPrincipalPayment: Double
    let tctdInterestPayment: Double
    let itemBreakdowns: [DebtItemPeriodPayment]
}

struct DebtScheduleResult: Codable, Equatable {
    let totalInitialDebt: Double
    let bankInitialDebt: Double
    let tctdInitialDebt: Double
    let totalInterest: Double
    let bankTotalInterest: Double
    let tctdTotalInterest: Double
    let totalPayment: Double
    let firstMonthPayment: Double
    let maxMonthPayment: Double
    let weightedAverageRate: Double
    let totalMonths: Int
    let schedule: [DebtPeriodBreakdown]
}

struct SavedDebtParams: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var customerName: String = ""
    var startDateStr: String = DateTimeUtils.todayString()
    var bankRate: Double = 10.5
    var isBankRateMonthly: Bool = false
    var tctdRate: Double = 28.0
    var isTctdRateMonthly: Bool = false
    var calculationMethod: LoanMethod = .reducingBalance
    var items: [DebtSourceItem] = []
    var savedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
}

