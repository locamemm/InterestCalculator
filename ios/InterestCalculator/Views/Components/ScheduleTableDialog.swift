//
//  ScheduleTableDialog.swift
//  InterestCalculator
//

import SwiftUI

struct LoanScheduleSheet: View {
    @Environment(\.dismiss) private var dismiss
    let title: String
    let customerName: String
    let result: LoanCalculationResult
    var onShare: (() -> Void)? = nil
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Summary Bar
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tổng gốc:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.totalPrincipal))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(AppTheme.primary)
                    }
                    
                    Divider().frame(height: 28)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tổng lãi:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.totalInterest))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(AppTheme.orange)
                    }
                    
                    Divider().frame(height: 28)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tổng gốc + lãi:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.totalPayment))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                
                // Table Header
                HStack {
                    Text("Kỳ")
                        .frame(width: 32, alignment: .leading)
                    Text("Ngày trả")
                        .frame(width: 76, alignment: .leading)
                    Text("Tiền gốc")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Tiền lãi")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Tổng trả")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Dư nợ")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.gray.opacity(0.08))
                
                // Schedule Items List
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(result.schedule) { item in
                            HStack {
                                Text("\(item.period)")
                                    .frame(width: 32, alignment: .leading)
                                    .foregroundColor(.secondary)
                                Text(DateTimeUtils.formatShortYearDate(item.paymentDate))
                                    .frame(width: 76, alignment: .leading)
                                Text(CurrencyUtils.formatScheduleVnd(item.principalPayment))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(AppTheme.primary)
                                Text(CurrencyUtils.formatScheduleVnd(item.interestPayment))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(AppTheme.orange)
                                Text(CurrencyUtils.formatScheduleVnd(item.totalPayment))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .fontWeight(.semibold)
                                Text(CurrencyUtils.formatScheduleVnd(item.remainingBalance))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(.secondary)
                            }
                            .font(.system(size: 11, design: .rounded))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 7)
                            .background(item.period % 2 == 0 ? Color.gray.opacity(0.03) : Color.clear)
                            
                            Divider()
                        }
                    }
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
                if let onShare = onShare {
                    ToolbarItem(placement: .primaryAction) {
                        Button {
                            dismiss()
                            onShare()
                        } label: {
                            Image(systemName: "square.and.arrow.up")
                        }
                    }
                }
            }
        }
    }
}

struct SavingsScheduleSheet: View {
    @Environment(\.dismiss) private var dismiss
    let title: String
    let customerName: String
    let result: SavingsCalculationResult
    var onShare: (() -> Void)? = nil
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Summary Bar
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tiền gốc:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.principalAmount))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(AppTheme.green)
                    }
                    
                    Divider().frame(height: 28)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tổng lãi:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.totalInterest))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(AppTheme.orange)
                    }
                    
                    Divider().frame(height: 28)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Tổng nhận khi đáo hạn:")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(CurrencyUtils.formatCompactVnd(result.totalMaturityAmount))
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                
                // Table Header
                HStack {
                    Text("Kỳ")
                        .frame(width: 32, alignment: .leading)
                    Text("Ngày nhận")
                        .frame(width: 80, alignment: .leading)
                    Text("Tiền lãi")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Gốc nhận")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Số dư sau kỳ")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.gray.opacity(0.08))
                
                // Schedule Items List
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(result.schedule) { item in
                            HStack {
                                Text("\(item.period)")
                                    .frame(width: 32, alignment: .leading)
                                    .foregroundColor(.secondary)
                                Text(DateTimeUtils.formatShortYearDate(item.payoutDate))
                                    .frame(width: 80, alignment: .leading)
                                Text(CurrencyUtils.formatScheduleVnd(item.interestAmount))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(AppTheme.orange)
                                    .fontWeight(.semibold)
                                Text(item.principalAmount > 0 ? CurrencyUtils.formatScheduleVnd(item.principalAmount) : "-")
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(AppTheme.green)
                                Text(CurrencyUtils.formatScheduleVnd(item.balanceAfter))
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .foregroundColor(.secondary)
                            }
                            .font(.system(size: 11, design: .rounded))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 7)
                            .background(item.period % 2 == 0 ? Color.gray.opacity(0.03) : Color.clear)
                            
                            Divider()
                        }
                    }
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
                if let onShare = onShare {
                    ToolbarItem(placement: .primaryAction) {
                        Button {
                            dismiss()
                            onShare()
                        } label: {
                            Image(systemName: "square.and.arrow.up")
                        }
                    }
                }
            }
        }
    }
}
