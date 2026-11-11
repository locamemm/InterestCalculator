//
//  KpiHeaderBanner.swift
//  InterestCalculator
//

import SwiftUI

struct KpiHeaderBanner: View {
    let kpi: KpiProfile
    let loans: [LoanProfile]
    let savings: [SavingsProfile]
    let onUpdateKpi: (KpiProfile) -> Void
    
    @State private var isMinimized: Bool = true
    @State private var showEditDialog: Bool = false
    
    // KPI actuals
    private var totalDisbursed: Double {
        loans.filter { $0.status == .disbursed }.reduce(0.0) { $0 + $1.principalAmount }
    }
    private var disbursedLoanCount: Int {
        loans.filter { $0.status == .disbursed }.count
    }
    private var totalSavings: Double {
        savings.filter { $0.status != .settled }.reduce(0.0) { $0 + $1.depositAmount }
    }
    private var activeSavingsCount: Int {
        savings.filter { $0.status != .settled }.count
    }
    private var expiringSavingsCount: Int {
        savings.filter { $0.status == .expiringSoon }.count
    }
    
    private var loanProgress: Double {
        guard kpi.loanDisbursementTarget > 0 else { return 0 }
        return min(1.0, max(0.0, totalDisbursed / kpi.loanDisbursementTarget))
    }
    
    private var savingsProgress: Double {
        guard kpi.savingsMobilizationTarget > 0 else { return 0 }
        return min(1.0, max(0.0, totalSavings / kpi.savingsMobilizationTarget))
    }
    
    var body: some View {
        VStack(spacing: 8) {
            // Header Bar
            HStack {
                HStack(spacing: 10) {
                    ZStack {
                        Circle()
                            .fill(AppTheme.primaryContainer)
                            .frame(width: 36, height: 36)
                        Image(systemName: "building.columns.fill")
                            .foregroundColor(AppTheme.primary)
                            .font(.system(size: 16))
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(kpi.staffName)
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                            .lineLimit(1)
                        Text(kpi.bankName)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                
                Spacer()
                
                HStack(spacing: 4) {
                    if expiringSavingsCount > 0 {
                        HStack(spacing: 2) {
                            Image(systemName: "bell.badge.fill")
                                .foregroundColor(AppTheme.orange)
                                .font(.system(size: 12))
                            Text("\(expiringSavingsCount) sổ")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(AppTheme.orange)
                        }
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(AppTheme.orangeContainer)
                        .cornerRadius(12)
                    }
                    
                    Button {
                        showEditDialog = true
                    } label: {
                        Image(systemName: "pencil")
                            .font(.system(size: 14))
                            .foregroundColor(AppTheme.primary)
                            .padding(6)
                    }
                    
                    Button {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            isMinimized.toggle()
                        }
                    } label: {
                        Image(systemName: isMinimized ? "chevron.down" : "chevron.up")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.secondary)
                            .padding(6)
                    }
                }
            }
            .contentShape(Rectangle())
            .onTapGesture {
                withAnimation(.easeInOut(duration: 0.2)) {
                    isMinimized.toggle()
                }
            }
            
            // Collapsible KPI Progress
            if !isMinimized {
                VStack(spacing: 10) {
                    Divider()
                    
                    // Loan KPI
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Label("Cho vay (GN)", systemImage: "banknote")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(AppTheme.primary)
                            Spacer()
                            Text("\(CurrencyUtils.formatCompactVnd(totalDisbursed)) / \(CurrencyUtils.formatCompactVnd(kpi.loanDisbursementTarget))")
                                .font(.caption)
                                .fontWeight(.bold)
                            Text("(\(Int(loanProgress * 100))%)")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        ProgressView(value: loanProgress)
                            .tint(AppTheme.primary)
                    }
                    
                    // Savings KPI
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Label("Huy động tiết kiệm", systemImage: "chart.line.uptrend.xyaxis")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(AppTheme.green)
                            Spacer()
                            Text("\(CurrencyUtils.formatCompactVnd(totalSavings)) / \(CurrencyUtils.formatCompactVnd(kpi.savingsMobilizationTarget))")
                                .font(.caption)
                                .fontWeight(.bold)
                            Text("(\(Int(savingsProgress * 100))%)")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        ProgressView(value: savingsProgress)
                            .tint(AppTheme.green)
                    }
                }
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
        .padding(.horizontal, 12)
        .padding(.top, 6)
        .sheet(isPresented: $showEditDialog) {
            EditKpiSheet(kpi: kpi, onSave: onUpdateKpi)
        }
    }
}

private struct EditKpiSheet: View {
    @Environment(\.dismiss) private var dismiss
    @State private var staffName: String
    @State private var bankName: String
    @State private var loanTargetText: String
    @State private var savingsTargetText: String
    
    let onSave: (KpiProfile) -> Void
    
    init(kpi: KpiProfile, onSave: @escaping (KpiProfile) -> Void) {
        self.onSave = onSave
        _staffName = State(initialValue: kpi.staffName)
        _bankName = State(initialValue: kpi.bankName)
        _loanTargetText = State(initialValue: String(format: "%.0f", kpi.loanDisbursementTarget))
        _savingsTargetText = State(initialValue: String(format: "%.0f", kpi.savingsMobilizationTarget))
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin nhân sự") {
                    TextField("Họ tên chuyên viên", text: $staffName)
                    TextField("Tên ngân hàng / Chi nhánh", text: $bankName)
                }
                
                Section("Chỉ tiêu KPI (VNĐ)") {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Chỉ tiêu giải ngân vay")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        TextField("Số tiền", text: $loanTargetText)
                            .keyboardType(.numberPad)
                        let amt = CurrencyUtils.parseAmount(loanTargetText)
                        if amt > 0 {
                            Text(CurrencyUtils.amountToWords(Int64(amt)))
                                .font(.caption2)
                                .foregroundColor(AppTheme.primary)
                        }
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Chỉ tiêu huy động tiết kiệm")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        TextField("Số tiền", text: $savingsTargetText)
                            .keyboardType(.numberPad)
                        let amt = CurrencyUtils.parseAmount(savingsTargetText)
                        if amt > 0 {
                            Text(CurrencyUtils.amountToWords(Int64(amt)))
                                .font(.caption2)
                                .foregroundColor(AppTheme.green)
                        }
                    }
                }
            }
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle("Cập nhật KPI cá nhân")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        let updated = KpiProfile(
                            staffName: staffName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Chuyên viên QHKH" : staffName,
                            bankName: bankName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Ngân hàng Thương mại" : bankName,
                            loanDisbursementTarget: CurrencyUtils.parseAmount(loanTargetText),
                            savingsMobilizationTarget: CurrencyUtils.parseAmount(savingsTargetText)
                        )
                        onSave(updated)
                        dismiss()
                    }
                    .fontWeight(.bold)
                }
            }
            .keyboardDoneToolbar()
        }
    }
}

