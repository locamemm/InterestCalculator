//
//  LoanListView.swift
//  InterestCalculator
//

import SwiftUI

struct LoanListView: View {
    let loans: [LoanProfile]
    let onAddLoan: (LoanProfile) -> Void
    let onUpdateLoan: (LoanProfile) -> Void
    let onDeleteLoan: (String) -> Void
    
    @State private var searchQuery: String = ""
    @State private var selectedStatusFilter: LoanStatus? = nil
    
    // Sheets & Dialogs
    @State private var loanToEdit: LoanProfile? = nil
    @State private var loanScheduleToShow: LoanProfile? = nil
    @State private var shareQuoteText: String? = nil
    @State private var loanToDelete: LoanProfile? = nil
    @State private var showDeleteAlert: Bool = false
    @State private var showCreateNewSheet: Bool = false
    
    private var filteredLoans: [LoanProfile] {
        loans.filter { loan in
            let matchesSearch = searchQuery.isEmpty ||
                loan.customerName.localizedCaseInsensitiveContains(searchQuery) ||
                loan.customerPhone.localizedCaseInsensitiveContains(searchQuery) ||
                loan.loanPurpose.localizedCaseInsensitiveContains(searchQuery)
            let matchesStatus = selectedStatusFilter == nil || loan.status == selectedStatusFilter
            return matchesSearch && matchesStatus
        }
    }
    
    var body: some View {
        VStack(spacing: 8) {
            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("Tìm kiếm tên, SĐT, mục đích vay...", text: $searchQuery)
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
            
            // Status Filter Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Button {
                        selectedStatusFilter = nil
                    } label: {
                        Text("Tất cả (\(loans.count))")
                            .font(.caption)
                            .fontWeight(selectedStatusFilter == nil ? .bold : .medium)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(selectedStatusFilter == nil ? AppTheme.primary : Color.gray.opacity(0.12))
                            .foregroundColor(selectedStatusFilter == nil ? .white : .primary)
                            .cornerRadius(10)
                    }
                    
                    ForEach(LoanStatus.allCases, id: \.self) { st in
                        let count = loans.filter { $0.status == st }.count
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
            
            // List of Loans
            if filteredLoans.isEmpty {
                VStack(spacing: 12) {
                    Spacer().frame(height: 40)
                    Image(systemName: "doc.text.magnifyingglass")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary.opacity(0.6))
                    Text("Không tìm thấy hồ sơ vay nào")
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
                        ForEach(filteredLoans) { loan in
                            LoanCardView(
                                loan: loan,
                                onSchedule: { loanScheduleToShow = loan },
                                onShare: {
                                    let res = LoanCalculator.calculate(
                                        principalAmount: loan.principalAmount,
                                        termMonths: loan.termMonths,
                                        baseInterestRate: loan.interestRate,
                                        hasPreferential: loan.hasPreferential,
                                        preferentialMonths: loan.preferentialMonths,
                                        preferentialRate: loan.preferentialRate,
                                        gracePeriodMonths: loan.gracePeriodMonths,
                                        method: loan.calculationMethod,
                                        startDateStr: loan.disbursementDate,
                                        repaymentPeriodMonths: loan.repaymentPeriodMonths
                                    )
                                    shareQuoteText = LoanCalculator.generateConsultationText(
                                        customerName: loan.customerName,
                                        principalAmount: loan.principalAmount,
                                        termMonths: loan.termMonths,
                                        baseRate: loan.interestRate,
                                        hasPreferential: loan.hasPreferential,
                                        preferentialMonths: loan.preferentialMonths,
                                        preferentialRate: loan.preferentialRate,
                                        method: loan.calculationMethod,
                                        result: res,
                                        startDateStr: loan.disbursementDate,
                                        repaymentPeriodMonths: loan.repaymentPeriodMonths
                                    )
                                },
                                onEdit: { loanToEdit = loan },
                                onDelete: {
                                    loanToDelete = loan
                                    showDeleteAlert = true
                                }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }
            }
        }
        .background(AppTheme.background)
        .sheet(item: $loanToEdit) { loan in
            SaveLoanSheet(profile: loan, onSave: onUpdateLoan)
        }
        .sheet(item: $loanScheduleToShow) { loan in
            let res = LoanCalculator.calculate(
                principalAmount: loan.principalAmount,
                termMonths: loan.termMonths,
                baseInterestRate: loan.interestRate,
                hasPreferential: loan.hasPreferential,
                preferentialMonths: loan.preferentialMonths,
                preferentialRate: loan.preferentialRate,
                gracePeriodMonths: loan.gracePeriodMonths,
                method: loan.calculationMethod,
                startDateStr: loan.disbursementDate,
                repaymentPeriodMonths: loan.repaymentPeriodMonths
            )
            LoanScheduleSheet(
                title: "Lịch trả nợ - \(loan.customerName)",
                customerName: loan.customerName,
                result: res
            )
        }
        .sheet(isPresented: Binding(
            get: { shareQuoteText != nil },
            set: { if !$0 { shareQuoteText = nil } }
        )) {
            if let text = shareQuoteText {
                ShareQuoteSheet(title: "Tư vấn hồ sơ vay", quoteText: text)
            }
        }
        .alert("Xác nhận xóa hồ sơ", isPresented: $showDeleteAlert) {
            Button("Xóa", role: .destructive) {
                if let id = loanToDelete?.id {
                    onDeleteLoan(id)
                }
                loanToDelete = nil
            }
            Button("Hủy", role: .cancel) {
                loanToDelete = nil
            }
        } message: {
            Text("Bạn có chắc chắn muốn xóa hồ sơ vay của \(loanToDelete?.customerName ?? "") không? Thao tác này không thể hoàn tác.")
        }
    }
}

private struct LoanCardView: View {
    let loan: LoanProfile
    let onSchedule: () -> Void
    let onShare: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    
    private var estimateFirstPayment: Double {
        let res = LoanCalculator.calculate(
            principalAmount: loan.principalAmount,
            termMonths: loan.termMonths,
            baseInterestRate: loan.interestRate,
            hasPreferential: loan.hasPreferential,
            preferentialMonths: loan.preferentialMonths,
            preferentialRate: loan.preferentialRate,
            gracePeriodMonths: loan.gracePeriodMonths,
            method: loan.calculationMethod,
            startDateStr: loan.disbursementDate,
            repaymentPeriodMonths: loan.repaymentPeriodMonths
        )
        return res.firstMonthPayment
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Header Row: Customer Name & Status Badge
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(loan.customerName)
                        .font(.headline)
                        .fontWeight(.bold)
                    Text(loan.loanPurpose.isEmpty ? "Vay vốn" : loan.loanPurpose)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Text(loan.status.title)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(loan.status.color)
                    .cornerRadius(8)
            }
            
            // Phone Row with quick Call/SMS
            if !loan.customerPhone.isEmpty {
                HStack(spacing: 8) {
                    Image(systemName: "phone.fill")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(loan.customerPhone)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    if let callUrl = URL(string: "tel://\(loan.customerPhone)") {
                        Link(destination: callUrl) {
                            Image(systemName: "phone.circle.fill")
                                .font(.title3)
                                .foregroundColor(AppTheme.green)
                        }
                    }
                    
                    if let smsUrl = URL(string: "sms:\(loan.customerPhone)") {
                        Link(destination: smsUrl) {
                            Image(systemName: "message.circle.fill")
                                .font(.title3)
                                .foregroundColor(AppTheme.primary)
                        }
                    }
                }
            }
            
            Divider()
            
            // Grid of Financial Details
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Số tiền vay")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text(CurrencyUtils.formatCompactVnd(loan.principalAmount))
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundColor(AppTheme.primary)
                }
                
                Spacer()
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("Thời hạn")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("\(loan.termMonths) tháng")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
                
                Spacer()
                
                VStack(alignment: .trailing, spacing: 2) {
                    Text("Lãi suất")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("\(String(format: "%.1f", loan.interestRate))%/năm")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(AppTheme.orange)
                }
            }
            
            // Estimated payment & Date
            HStack {
                Text("Trả kỳ 1: ~ \(CurrencyUtils.formatCompactVnd(estimateFirstPayment))")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(AppTheme.primary)
                
                Spacer()
                
                if !loan.disbursementDate.isEmpty {
                    Text("GN: \(loan.disbursementDate)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
            
            if !loan.note.isEmpty {
                Text("Ghi chú: \(loan.note)")
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
                    Label("Bảng tính", systemImage: "tablecells")
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

