//
//  SaveLoanDialog.swift
//  InterestCalculator
//

import SwiftUI

struct SaveLoanSheet: View {
    @Environment(\.dismiss) private var dismiss
    let initialProfile: LoanProfile?
    let onSave: (LoanProfile) -> Void
    
    @State private var customerName: String
    @State private var customerPhone: String
    @State private var loanPurpose: String
    @State private var principalAmount: Double
    @State private var termMonths: Int
    @State private var repaymentPeriodMonths: Int
    @State private var interestRate: Double
    @State private var hasPreferential: Bool
    @State private var preferentialMonths: Int
    @State private var preferentialRate: Double
    @State private var gracePeriodMonths: Int
    @State private var calculationMethod: LoanMethod
    @State private var disbursementDate: String
    @State private var status: LoanStatus
    @State private var note: String
    
    init(profile: LoanProfile?, defaultLoan: LoanProfile? = nil, onSave: @escaping (LoanProfile) -> Void) {
        self.onSave = onSave
        self.initialProfile = profile
        let p = profile ?? defaultLoan ?? LoanProfile(
            customerName: "",
            principalAmount: 1_000_000_000.0,
            termMonths: 120,
            interestRate: 9.5
        )
        _customerName = State(initialValue: p.customerName)
        _customerPhone = State(initialValue: p.customerPhone)
        _loanPurpose = State(initialValue: p.loanPurpose)
        _principalAmount = State(initialValue: p.principalAmount)
        _termMonths = State(initialValue: p.termMonths)
        _repaymentPeriodMonths = State(initialValue: p.repaymentPeriodMonths)
        _interestRate = State(initialValue: p.interestRate)
        _hasPreferential = State(initialValue: p.hasPreferential)
        _preferentialMonths = State(initialValue: p.preferentialMonths)
        _preferentialRate = State(initialValue: p.preferentialRate)
        _gracePeriodMonths = State(initialValue: p.gracePeriodMonths)
        _calculationMethod = State(initialValue: p.calculationMethod)
        _disbursementDate = State(initialValue: p.disbursementDate.isEmpty ? DateTimeUtils.todayString() : p.disbursementDate)
        _status = State(initialValue: p.status)
        _note = State(initialValue: p.note)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin khách hàng") {
                    TextField("Họ và tên khách hàng *", text: $customerName)
                    TextField("Số điện thoại liên hệ", text: $customerPhone)
                        .keyboardType(.phonePad)
                    TextField("Mục đích vay (VD: Mua nhà, Mua xe)", text: $loanPurpose)
                }
                
                Section("Khoản vay") {
                    MoneyInputField(label: "Số tiền vay", amount: $principalAmount)
                    TermInputField(label: "Thời hạn vay", termMonths: $termMonths)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Lãi suất áp dụng (%/năm)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        TextField("0.0", value: $interestRate, format: .number)
                            .keyboardType(.decimalPad)
                            .font(.headline)
                    }
                    
                    Picker("Phương thức tính", selection: $calculationMethod) {
                        ForEach(LoanMethod.allCases, id: \.self) { m in
                            Text(m.title).tag(m)
                        }
                    }
                    
                    DateInputField(label: "Ngày giải ngân dự kiến", dateStr: $disbursementDate)
                }
                
                Section("Chính sách & Ưu đãi") {
                    Toggle("Ưu đãi lãi suất", isOn: $hasPreferential)
                    if hasPreferential {
                        Stepper("Số tháng ưu đãi: \(preferentialMonths) tháng", value: $preferentialMonths, in: 1...termMonths)
                        HStack {
                            Text("Lãi suất ưu đãi (%/năm):")
                            Spacer()
                            TextField("0.0", value: $preferentialRate, format: .number)
                                .keyboardType(.decimalPad)
                                .multilineTextAlignment(.trailing)
                                .frame(width: 80)
                        }
                    }
                    
                    Stepper("Ân hạn gốc: \(gracePeriodMonths) tháng", value: $gracePeriodMonths, in: 0...termMonths)
                }
                
                Section("Trạng thái & Ghi chú") {
                    Picker("Trạng thái hồ sơ", selection: $status) {
                        ForEach(LoanStatus.allCases, id: \.self) { s in
                            Text(s.title).tag(s)
                        }
                    }
                    
                    TextField("Ghi chú tiến độ, lịch hẹn...", text: $note, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(initialProfile != nil ? "Sửa hồ sơ vay" : "Lưu hồ sơ vay mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        let finalName = customerName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Khách hàng" : customerName
                        var updated = initialProfile ?? LoanProfile(
                            customerName: finalName,
                            principalAmount: principalAmount,
                            termMonths: termMonths,
                            interestRate: interestRate
                        )
                        updated.customerName = finalName
                        updated.customerPhone = customerPhone
                        updated.loanPurpose = loanPurpose
                        updated.principalAmount = principalAmount
                        updated.termMonths = termMonths
                        updated.repaymentPeriodMonths = repaymentPeriodMonths
                        updated.interestRate = interestRate
                        updated.hasPreferential = hasPreferential
                        updated.preferentialMonths = preferentialMonths
                        updated.preferentialRate = preferentialRate
                        updated.gracePeriodMonths = gracePeriodMonths
                        updated.calculationMethod = calculationMethod
                        updated.disbursementDate = disbursementDate
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

