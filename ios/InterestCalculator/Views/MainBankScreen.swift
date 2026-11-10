//
//  MainBankScreen.swift
//  InterestCalculator
//

import SwiftUI

struct MainBankScreen: View {
    @ObservedObject var repository: BankDataRepository
    
    @State private var mainTabIndex: Int = 0
    @State private var loanSubTabIndex: Int = 0
    @State private var savingsSubTabIndex: Int = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Top Custom Header Bar
                HStack(spacing: 10) {
                    Image(systemName: "building.columns.fill")
                        .foregroundColor(AppTheme.primary)
                        .font(.system(size: 24))
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("TRỢ LÝ NGÂN HÀNG")
                            .font(.system(size: 17, weight: .black, design: .rounded))
                            .foregroundColor(AppTheme.primary)
                        Text("Tín Dụng & Huy Động Tiết Kiệm")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(Color(UIColor.systemBackground))
                
                // KPI Header Banner
                KpiHeaderBanner(
                    kpi: repository.kpi,
                    loans: repository.loans,
                    savings: repository.savings,
                    onUpdateKpi: { updated in
                        repository.updateKpi(updated)
                    }
                )
                
                // 3 Main Horizontal Segmented Tabs
                HStack(spacing: 4) {
                    TabButton(
                        title: "CHO VAY",
                        systemImage: "banknote.fill",
                        isSelected: mainTabIndex == 0,
                        activeColor: AppTheme.primary
                    ) {
                        mainTabIndex = 0
                    }
                    
                    TabButton(
                        title: "TIẾT KIỆM",
                        systemImage: "chart.line.uptrend.xyaxis",
                        isSelected: mainTabIndex == 1,
                        activeColor: AppTheme.green
                    ) {
                        mainTabIndex = 1
                    }
                    
                    TabButton(
                        title: "TÍNH DƯ NỢ",
                        systemImage: "creditcard.fill",
                        isSelected: mainTabIndex == 2,
                        activeColor: AppTheme.orange
                    ) {
                        mainTabIndex = 2
                    }
                }
                .padding(.horizontal, 12)
                .padding(.top, 8)
                .padding(.bottom, 4)
                
                // Active Screen
                Group {
                    switch mainTabIndex {
                    case 0:
                        LoanTabScreen(
                            repository: repository,
                            selectedSubTab: $loanSubTabIndex
                        )
                    case 1:
                        SavingsTabScreen(
                            repository: repository,
                            selectedSubTab: $savingsSubTabIndex
                        )
                    default:
                        DebtCalculatorView(
                            repository: repository
                        )
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .background(AppTheme.background)
            .navigationBarHidden(true)
        }
    }
}

private struct TabButton: View {
    let title: String
    let systemImage: String
    let isSelected: Bool
    let activeColor: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: systemImage)
                    .font(.system(size: 13, weight: .bold))
                Text(title)
                    .font(.system(size: 12, weight: .heavy, design: .rounded))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .background(isSelected ? activeColor.opacity(0.12) : Color.clear)
            .foregroundColor(isSelected ? activeColor : .secondary)
            .cornerRadius(10)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(isSelected ? activeColor : Color.clear, lineWidth: 1.5)
            )
        }
    }
}

