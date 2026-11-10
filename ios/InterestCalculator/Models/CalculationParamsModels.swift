//
//  CalculationParamsModels.swift
//  InterestCalculator
//

import Foundation

struct SavedLoanParams: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var customerName: String = ""
    var principalAmount: Double = 1_000_000_000.0
    var termMonths: Int = 120
    var repaymentPeriodMonths: Int = 1
    var baseRate: Double = 9.5
    var isBaseRateMonthly: Bool = false
    var startDateStr: String = DateTimeUtils.todayString()
    var hasPreferential: Bool = true
    var preferentialMonths: Int = 12
    var preferentialRate: Double = 6.8
    var hasGracePeriod: Bool = false
    var gracePeriodMonths: Int = 6
    var selectedMethod: LoanMethod = .reducingBalance
    var savedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
}

struct SavedSavingsParams: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var name: String = ""
    var customerName: String = ""
    var depositAmount: Double = 500_000_000.0
    var termMonths: Int = 12
    var periodMonths: Int = 6
    var interestRate: Double = 5.8
    var startDateStr: String = DateTimeUtils.todayString()
    var paymentType: SavingsPaymentType = .endOfTerm
    var isCompounding: Bool = false
    var savedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
}
