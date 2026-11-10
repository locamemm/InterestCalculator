//
//  InterestCalculatorApp.swift
//  InterestCalculator
//

import SwiftUI

@main
struct InterestCalculatorApp: App {
    @StateObject private var repository = BankDataRepository()
    
    var body: some Scene {
        WindowGroup {
            MainBankScreen(repository: repository)
                .preferredColorScheme(.light)
        }
    }
}
