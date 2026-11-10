//
//  SavingsTabScreen.swift
//  InterestCalculator
//

import SwiftUI

struct SavingsTabScreen: View {
    @ObservedObject var repository: BankDataRepository
    @Binding var selectedSubTab: Int
    
    var body: some View {
        VStack(spacing: 0) {
            // Sub-tab Segmented Picker
            Picker("", selection: $selectedSubTab) {
                Label("Công cụ tính", systemImage: "plus.forwardslash.minus").tag(0)
                Label("Sổ tiết kiệm (\(repository.savings.count))", systemImage: "tray.fill").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color(UIColor.systemBackground))
            
            Divider()
            
            // Sub-screen
            if selectedSubTab == 0 {
                SavingsCalculatorView(
                    repository: repository,
                    onSaveSavings: { newSavings in
                        repository.addSavings(newSavings)
                    }
                )
            } else {
                SavingsListView(
                    savings: repository.savings,
                    onAddSavings: { repository.addSavings($0) },
                    onUpdateSavings: { repository.updateSavings($0) },
                    onDeleteSavings: { repository.deleteSavings(id: $0) }
                )
            }
        }
    }
}

