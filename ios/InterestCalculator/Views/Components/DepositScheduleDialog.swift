//
//  DepositScheduleDialog.swift
//  InterestCalculator
//

import SwiftUI

struct DepositScheduleSheet: View {
    @Environment(\.dismiss) private var dismiss
    let customerName: String
    let result: DepositScheduleResult
    var onShare: (() -> Void)? = nil
    
    @State private var expandedPeriod: Int? = nil
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Header Summary
                VStack(spacing: 8) {
                    HStack(spacing: 12) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tổng tiền gửi:")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Text(CurrencyUtils.formatCompactVnd(result.totalPrincipal))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.primary)
                        }
                        
                        Divider().frame(height: 28)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tổng tiền lãi:")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Text(CurrencyUtils.formatCompactVnd(result.totalInterest))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.teal)
                        }
                        
                        Divider().frame(height: 28)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Lãi bình quân:")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Text("\(String(format: "%.2f", result.weightedAverageRate))%/n")
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                        }
                        Spacer()
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                
                // Table Header
                HStack {
                    Text("Kỳ")
                        .frame(width: 28, alignment: .leading)
                    Text("Ngày")
                        .frame(width: 60, alignment: .leading)
                    Text("Tiền lãi")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Gốc đáo hạn")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Tổng nhận")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("Số dư gửi")
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    Text("")
                        .frame(width: 14)
                }
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.gray.opacity(0.08))
                
                // Rows
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(result.schedule) { item in
                            VStack(spacing: 0) {
                                HStack {
                                    Text("\(item.period)")
                                        .frame(width: 28, alignment: .leading)
                                        .foregroundColor(.secondary)
                                    Text(DateTimeUtils.formatShortYearDate(item.payoutDate))
                                        .frame(width: 60, alignment: .leading)
                                    Text(CurrencyUtils.formatScheduleVnd(item.interestPayout))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .foregroundColor(AppTheme.teal)
                                    Text(CurrencyUtils.formatScheduleVnd(item.principalPayout))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .foregroundColor(AppTheme.primary)
                                    Text(CurrencyUtils.formatScheduleVnd(item.totalPayout))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .fontWeight(.semibold)
                                    Text(CurrencyUtils.formatScheduleVnd(item.remainingBalance))
                                        .frame(maxWidth: .infinity, alignment: .trailing)
                                        .foregroundColor(.secondary)
                                    
                                    Image(systemName: expandedPeriod == item.period ? "chevron.up" : "chevron.down")
                                        .font(.system(size: 9))
                                        .foregroundColor(.secondary)
                                        .frame(width: 14)
                                }
                                .font(.system(size: 11, design: .rounded))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(item.period % 2 == 0 ? Color.gray.opacity(0.03) : Color.clear)
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    withAnimation(.easeInOut(duration: 0.15)) {
                                        if expandedPeriod == item.period {
                                            expandedPeriod = nil
                                        } else {
                                            expandedPeriod = item.period
                                        }
                                    }
                                }
                                
                                // Expandable Item Breakdown
                                if expandedPeriod == item.period {
                                    VStack(alignment: .leading, spacing: 6) {
                                        Text("Chi tiết các sổ tiền gửi trong kỳ \(item.period):")
                                            .font(.caption2)
                                            .fontWeight(.bold)
                                            .foregroundColor(.secondary)
                                        
                                        ForEach(item.itemBreakdowns) { b in
                                            HStack {
                                                Text(b.sourceType.shortTitle)
                                                    .font(.system(size: 9, weight: .bold))
                                                    .foregroundColor(.white)
                                                    .padding(.horizontal, 4)
                                                    .padding(.vertical, 2)
                                                    .background(b.sourceType.color)
                                                    .cornerRadius(4)
                                                Text(b.itemName)
                                                    .font(.caption2)
                                                    .lineLimit(1)
                                                Spacer()
                                                Text("L: \(CurrencyUtils.formatScheduleVnd(b.interestPayout))")
                                                    .font(.caption2)
                                                    .foregroundColor(AppTheme.teal)
                                                Text("G: \(CurrencyUtils.formatScheduleVnd(b.principalPayout))")
                                                    .font(.caption2)
                                                    .foregroundColor(AppTheme.primary)
                                                Text("T: \(CurrencyUtils.formatScheduleVnd(b.totalPayout))")
                                                    .font(.caption2)
                                                    .fontWeight(.semibold)
                                            }
                                            .padding(.horizontal, 6)
                                            .padding(.vertical, 3)
                                            .background(Color(UIColor.systemBackground))
                                            .cornerRadius(6)
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(AppTheme.teal.opacity(0.05))
                                }
                                
                                Divider()
                            }
                        }
                    }
                }
            }
            .navigationTitle("Lịch nhận lãi đa nguồn")
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
