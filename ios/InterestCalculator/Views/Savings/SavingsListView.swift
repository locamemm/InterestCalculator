//
//  SavingsListView.swift
//  InterestCalculator
//

import SwiftUI

struct SavingsListView: View {
    let savings: [SavingsProfile]
    let onAddSavings: (SavingsProfile) -> Void
    let onUpdateSavings: (SavingsProfile) -> Void
    let onDeleteSavings: (String) -> Void
    
    @State private var searchQuery: String = ""
    @State private var selectedStatusFilter: SavingsStatus? = nil
    
    // Sheets & Dialogs
    @State private var savingsToEdit: SavingsProfile? = nil
    @State private var savingsScheduleToShow: SavingsProfile? = nil
    @State private var shareQuoteText: String? = nil
    @State private var savingsToDelete: SavingsProfile? = nil
    @State private var showDeleteAlert: Bool = false
    
    private var filteredSavings: [SavingsProfile] {
        savings.filter { s in
            let matchesSearch = searchQuery.isEmpty ||
                s.customerName.localizedCaseInsensitiveContains(searchQuery) ||
                s.customerPhone.localizedCaseInsensitiveContains(searchQuery)
            let matchesStatus = selectedStatusFilter == nil || s.status == selectedStatusFilter
            return matchesSearch && matchesStatus
        }
    }
    
    var body: some View {
        VStack(spacing: 8) {
            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("Tìm kiếm tên người gửi, SĐT...", text: $searchQuery)
                if !searchQuery.isEmpty {
                    Button {
                        searchQuery = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(10)
            .background(Color(UIColor.secondarySystemGroupedBackground))
            .cornerRadius(12)
            .padding(.horizontal, 16)
            .padding(.top, 4)
            
            // Filter Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Button {
                        selectedStatusFilter = nil
                    } label: {
                        Text("Tất cả (\(savings.count))")
                            .font(.caption)
                            .fontWeight(selectedStatusFilter == nil ? .bold : .medium)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(selectedStatusFilter == nil ? AppTheme.green : Color.gray.opacity(0.12))
                            .foregroundColor(selectedStatusFilter == nil ? .white : .primary)
                            .cornerRadius(10)
                    }
                    
                    ForEach(SavingsStatus.allCases, id: \.self) { st in
                        let count = savings.filter { $0.status == st }.count
                        Button {
                            selectedStatusFilter = st
                        } label: {
                            Text("\(st.title) (\(count))")
                                .font(.caption)
                                .fontWeight(selectedStatusFilter == st ? .bold : .medium)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(selectedStatusFilter == st ? st.color : Color.gray.opacity(0.12))
                                .foregroundColor(selectedStatusFilter == st ? .white : .primary)
                                .cornerRadius(10)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            
            // List
            if filteredSavings.isEmpty {
                VStack(spacing: 12) {
                    Spacer().frame(height: 40)
                    Image(systemName: "tray.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary.opacity(0.6))
                    Text("Không tìm thấy sổ tiết kiệm nào")
                        .font(.headline)
                        .foregroundColor(.secondary)
                    Text("Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc trạng thái")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredSavings) { item in
                            SavingsCardView(
                                item: item,
                                onSchedule: { savingsScheduleToShow = item },
                                onShare: {
                                    let res = SavingsCalculator.calculate(
                                        depositAmount: item.depositAmount,
                                        termMonths: item.termMonths,
                                        annualInterestRate: item.interestRate,
                                        paymentType: item.interestPaymentType,
                                        isAutoRenewCompounding: item.isAutoRenewCompounding,
                                        startDateStr: item.depositDate,
                                        periodMonths: item.periodMonths
                                    )
                                    shareQuoteText = SavingsCalculator.generateConsultationText(
                                        customerName: item.customerName,
                                        depositAmount: item.depositAmount,
                                        termMonths: item.termMonths,
                                        interestRate: item.interestRate,
                                        paymentType: item.interestPaymentType,
                                        isAutoRenewCompounding: item.isAutoRenewCompounding,
                                        result: res,
                                        startDateStr: item.depositDate,
                                        periodMonths: item.periodMonths
                                    )
                                },
                                onEdit: { savingsToEdit = item },
                                onDelete: {
                                    savingsToDelete = item
                                    showDeleteAlert = true
                                }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
                .scrollDismissesKeyboard(.interactively)
            }
        }
        .background(AppTheme.background)
        .sheet(item: $savingsToEdit) { item in
            SaveSavingsSheet(profile: item, onSave: onUpdateSavings)
        }
        .sheet(item: $savingsScheduleToShow) { item in
            let res = SavingsCalculator.calculate(
                depositAmount: item.depositAmount,
                termMonths: item.termMonths,
                annualInterestRate: item.interestRate,
                paymentType: item.interestPaymentType,
                isAutoRenewCompounding: item.isAutoRenewCompounding,
                startDateStr: item.depositDate,
                periodMonths: item.periodMonths
            )
            SavingsScheduleSheet(
                title: "Lịch nhận lãi - \(item.customerName)",
                customerName: item.customerName,
                result: res
            )
        }
        .sheet(isPresented: Binding(
            get: { shareQuoteText != nil },
            set: { if !$0 { shareQuoteText = nil } }
        )) {
            if let text = shareQuoteText {
                ShareQuoteSheet(title: "Tư vấn sổ tiết kiệm", quoteText: text)
            }
        }
        .alert("Xác nhận xóa sổ", isPresented: $showDeleteAlert) {
            Button("Xóa", role: .destructive) {
                if let id = savingsToDelete?.id {
                    onDeleteSavings(id)
                }
                savingsToDelete = nil
            }
            Button("Hủy", role: .cancel) {
                savingsToDelete = nil
            }
        } message: {
            Text("Bạn có chắc chắn muốn xóa sổ tiết kiệm của \(savingsToDelete?.customerName ?? "") không? Thao tác này không thể hoàn tác.")
        }
    }
}

private struct SavingsCardView: View {
    let item: SavingsProfile
    let onSchedule: () -> Void
    let onShare: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    
    private var calculationResult: SavingsCalculationResult {
        SavingsCalculator.calculate(
            depositAmount: item.depositAmount,
            termMonths: item.termMonths,
            annualInterestRate: item.interestRate,
            paymentType: item.interestPaymentType,
            isAutoRenewCompounding: item.isAutoRenewCompounding,
            startDateStr: item.depositDate,
            periodMonths: item.periodMonths
        )
    }
    
    private var daysUntilMaturity: Int {
        DateTimeUtils.daysUntil(item.maturityDate)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Header: Name & Status Badges
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.customerName)
                        .font(.headline)
                        .fontWeight(.bold)
                    Text(item.interestPaymentType.title)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 4) {
                    Text(item.status.title)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(item.status.color)
                        .cornerRadius(8)
                    
                    if item.status != .settled && !item.maturityDate.isEmpty {
                        let days = daysUntilMaturity
                        Text(days > 0 ? "Còn \(days) ngày" : (days == 0 ? "Đáo hạn hôm nay" : "Quá hạn \(abs(days)) ngày"))
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(days <= 15 ? AppTheme.orange : .secondary)
                    }
                }
            }
            
            // Phone Row
            if !item.customerPhone.isEmpty {
                HStack(spacing: 8) {
                    Image(systemName: "phone.fill")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(item.customerPhone)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    if let callUrl = URL(string: "tel://\(item.customerPhone)") {
                        Link(destination: callUrl) {
                            Image(systemName: "phone.circle.fill")
                                .font(.title3)
                                .foregroundColor(AppTheme.green)
                        }
                    }
                    
                    if let smsUrl = URL(string: "sms:\(item.customerPhone)") {
                        Link(destination: smsUrl) {
                            Image(systemName: "message.circle.fill")
                                .font(.title3)
                                .foregroundColor(AppTheme.primary)
                        }
                    }
                }
            }
            
            Divider()
            
            // Financial details
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Số tiền gửi")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(CurrencyUtils.formatCompactVnd(item.depositAmount))
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundColor(AppTheme.green)
                }
                
                Spacer()
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("Thời hạn")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("\(item.termMonths) tháng")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 2) {
                    Text("Lãi suất")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("\(String(format: "%.1f", item.interestRate))%/năm")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(AppTheme.orange)
                }
            }
            
            HStack {
                Text("Lãi dự kiến: \(CurrencyUtils.formatCompactVnd(calculationResult.totalInterest))")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(AppTheme.green)
                
                Spacer()
                
                if !item.maturityDate.isEmpty {
                    Text("Đáo hạn: \(item.maturityDate)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
            
            if !item.note.isEmpty {
                Text("Ghi chú: \(item.note)")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
            
            Divider()
            
            // Action Buttons
            HStack(spacing: 8) {
                Button {
                    onSchedule()
                } label: {
                    Label("Lịch nhận lãi", systemImage: "tablecells")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .background(Color.gray.opacity(0.1))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
                
                Button {
                    onShare()
                } label: {
                    Label("Tư vấn", systemImage: "square.and.arrow.up")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .background(AppTheme.green.opacity(0.1))
                        .foregroundColor(AppTheme.green)
                        .cornerRadius(8)
                }
                
                Spacer()
                
                Button {
                    onEdit()
                } label: {
                    Image(systemName: "pencil")
                        .font(.caption)
                        .padding(8)
                        .background(Color.gray.opacity(0.1))
                        .foregroundColor(.primary)
                        .clipShape(Circle())
                }
                
                Button {
                    onDelete()
                } label: {
                    Image(systemName: "trash")
                        .font(.caption)
                        .padding(8)
                        .background(Color.red.opacity(0.1))
                        .foregroundColor(.red)
                        .clipShape(Circle())
                }
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)
    }
}

