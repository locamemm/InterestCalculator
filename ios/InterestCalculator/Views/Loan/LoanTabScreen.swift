//
//  LoanTabScreen.swift
//  InterestCalculator
//

import SwiftUI

struct LoanTabScreen: View {
    @ObservedObject var repository: BankDataRepository
    @Binding var selectedSubTab: Int
    
    var body: some View {
        VStack(spacing: 0) {
            // Sub-tab Segmented Picker
            Picker("", selection: $selectedSubTab) {
                Label("Công cụ tính", systemImage: "plus.forwardslash.minus").tag(0)
                Label("Hồ sơ vay (\(repository.loans.count))", systemImage: "doc.text.fill").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color(UIColor.systemBackground))
            
            Divider()
            
            // Sub-screen
            if selectedSubTab == 0 {
                LoanCalculatorView(
                    repository: repository,
                    onSaveLoan: { newLoan in
                        repository.addLoan(newLoan)
                    }
                )
            } else {
                LoanListView(
                    loans: repository.loans,
                    onAddLoan: { repository.addLoan($0) },
                    onUpdateLoan: { repository.updateLoan($0) },
                    onDeleteLoan: { repository.deleteLoan(id: $0) }
                )
            }
        }
    }
}
