//
//  SaveSavingsDialog.swift
//  InterestCalculator
//

import SwiftUI

struct SaveSavingsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let initialProfile: SavingsProfile?
    let onSave: (SavingsProfile) -> Void
    
    @State private var customerName: String
    @State private var customerPhone: String
    @State private var depositAmount: Double
    @State private var termMonths: Int
    @State private var interestRate: Double
    @State private var interestPaymentType: SavingsPaymentType
    @State private var isAutoRenewCompounding: Bool
    @State private var depositDate: String
    @State private var status: SavingsStatus
    @State private var note: String
    
    init(profile: SavingsProfile?, defaultSavings: SavingsProfile? = nil, onSave: @escaping (SavingsProfile) -> Void) {
        self.onSave = onSave
        self.initialProfile = profile
        let p = profile ?? defaultSavings ?? SavingsProfile(
            customerName: "",
            depositAmount: 500_000_000.0,
            termMonths: 12,
            interestRate: 5.8
        )
        _customerName = State(initialValue: p.customerName)
        _customerPhone = State(initialValue: p.customerPhone)
        _depositAmount = State(initialValue: p.depositAmount)
        _termMonths = State(initialValue: p.termMonths)
        _interestRate = State(initialValue: p.interestRate)
        _interestPaymentType = State(initialValue: p.interestPaymentType)
        _isAutoRenewCompounding = State(initialValue: p.isAutoRenewCompounding)
        _depositDate = State(initialValue: p.depositDate.isEmpty ? DateTimeUtils.todayString() : p.depositDate)
        _status = State(initialValue: p.status)
        _note = State(initialValue: p.note)
    }
    
    private var computedMaturityDate: String {
        DateTimeUtils.addMonths(depositDate, termMonths)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin khách hàng") {
                    TextField("Họ và tên người gửi tiền *", text: $customerName)
                    TextField("Số điện thoại liên hệ", text: $customerPhone)
                        .keyboardType(.phonePad)
                }
                
                Section("Thông tin sổ tiết kiệm") {
                    MoneyInputField(label: "Số tiền gửi", amount: $depositAmount, accentColor: AppTheme.green)
                    TermInputField(label: "Thời hạn gửi", termMonths: $termMonths, accentColor: AppTheme.green)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Lãi suất huy động (%/năm)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        TextField("0.0", value: $interestRate, format: .number)
                            .keyboardType(.decimalPad)
                            .font(.headline)
                    }
                    
                    Picker("Hình thức trả lãi", selection: $interestPaymentType) {
                        ForEach(SavingsPaymentType.allCases, id: \.self) { pt in
                            Text(pt.title).tag(pt)
                        }
                    }
                    
                    if interestPaymentType == .endOfTerm {
                        Toggle("Tự động quay vòng lãi kép (nhập gốc)", isOn: $isAutoRenewCompounding)
                    }
                    
                    DateInputField(label: "Ngày gửi tiền", dateStr: $depositDate, accentColor: AppTheme.green)
                    
                    HStack {
                        Text("Ngày đáo hạn dự kiến:")
                            .foregroundColor(.secondary)
                        Spacer()
                        Text(computedMaturityDate)
                            .fontWeight(.bold)
                            .foregroundColor(AppTheme.green)
                    }
                }
                
                Section("Trạng thái & Ghi chú") {
                    Picker("Trạng thái sổ", selection: $status) {
                        ForEach(SavingsStatus.allCases, id: \.self) { s in
                            Text(s.title).tag(s)
                        }
                    }
                    
                    TextField("Ghi chú hẹn khách, tái tục...", text: $note, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(initialProfile != nil ? "Sửa sổ tiết kiệm" : "Lưu sổ tiết kiệm mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        let finalName = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Khách gửi" : customerName
                        var updated = initialProfile ?? SavingsProfile(
                            customerName: finalName,
                            depositAmount: depositAmount,
                            termMonths: termMonths,
                            interestRate: interestRate
                        )
                        updated.customerName = finalName
                        updated.customerPhone = customerPhone
                        updated.depositAmount = depositAmount
                        updated.termMonths = termMonths
                        updated.periodMonths = termMonths
                        updated.interestRate = interestRate
                        updated.interestPaymentType = interestPaymentType
                        updated.isAutoRenewCompounding = isAutoRenewCompounding
                        updated.depositDate = depositDate
                        updated.maturityDate = computedMaturityDate
                        updated.status = status
                        updated.note = note
                        
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

