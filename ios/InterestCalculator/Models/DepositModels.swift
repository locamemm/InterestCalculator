//
//  DepositModels.swift
//  InterestCalculator
//

import Foundation
import SwiftUI

enum DepositSourceType: String, CaseIterable, Codable {
    case counter = "COUNTER"
    case online = "ONLINE"
    
    var title: String {
        switch self {
        case .counter: return "Tại quầy"
        case .online: return "Gửi Online"
        }
    }
    
    var shortTitle: String {
        switch self {
        case .counter: return "Quầy"
        case .online: return "Online"
        }
    }
    
    var color: Color {
        switch self {
        case .counter: return AppTheme.primary
        case .online: return AppTheme.green
        }
    }
}

struct DepositSourceItem: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var sourceType: DepositSourceType = .counter
    var principalAmount: Double = 0.0
    var maturityDate: String = ""
    var customRate: Double? = nil
}

struct DepositItemPeriodPayout: Identifiable, Codable, Equatable {
    var id: String { itemId }
    let itemId: String
    let itemName: String
    let sourceType: DepositSourceType
    let interestPayout: Double
    let principalPayout: Double
    let totalPayout: Double
    let remainingBalance: Double
}

struct DepositPeriodBreakdown: Identifiable, Codable, Equatable {
    var id: Int { period }
    let period: Int
    let payoutDate: String
    let interestPayout: Double
    let principalPayout: Double
    let totalPayout: Double
    let remainingBalance: Double
    let counterInterestPayout: Double
    let onlineInterestPayout: Double
    let itemBreakdowns: [DepositItemPeriodPayout]
}

struct DepositScheduleResult: Codable, Equatable {
    let totalPrincipal: Double
    let counterPrincipal: Double
    let onlinePrincipal: Double
    let totalInterest: Double
    let counterTotalInterest: Double
    let onlineTotalInterest: Double
    let totalMaturityAmount: Double
    let firstMonthPayout: Double
    let maxMonthPayout: Double
    let weightedAverageRate: Double
    let totalMonths: Int
    let schedule: [DepositPeriodBreakdown]
}

struct SavedDepositParams: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var customerName: String = ""
    var startDateStr: String = DateTimeUtils.todayString()
    var counterRate: Double = 5.2
    var isCounterRateMonthly: Bool = false
    var onlineRate: Double = 6.0
    var isOnlineRateMonthly: Bool = false
    var paymentType: SavingsPaymentType = .endOfTerm
    var isCompounding: Bool = false
    var items: [DepositSourceItem] = []
    var savedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
}

