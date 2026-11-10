//
//  DebtCalculatorView.swift
//  InterestCalculator
//

import SwiftUI

struct DebtCalculatorView: View {
    @ObservedObject var repository: BankDataRepository
    
    @State private var customerName: String = ""
    @State private var startDateStr: String = DateTimeUtils.todayString()
    @State private var bankRate: Double = 10.5
    @State private var isBankRateMonthly: Bool = false
    @State private var tctdRate: Double = 28.0
    @State private var isTctdRateMonthly: Bool = false
    @State private var selectedMethod: LoanMethod = .reducingBalance
    @State private var items: [DebtSourceItem] = []
    
    // Sheets
    @State private var showScheduleSheet: Bool = false
    @State private var showShareSheet: Bool = false
    @State private var showPresetsSheet: Bool = false
    @State private var itemToEdit: DebtSourceItem? = nil
    @State private var showAddItemSheet: Bool = false
    
    private var calculationResult: DebtScheduleResult {
        let actualBankRate = isBankRateMonthly ? bankRate * 12.0 : bankRate
        let actualTctdRate = isTctdRateMonthly ? tctdRate * 12.0 : tctdRate
        
        return DebtCalculator.calculate(
            items: items,
            startDateStr: startDateStr,
            bankRate: actualBankRate,
            tctdRate: actualTctdRate,
            method: selectedMethod
        )
    }
    
    private var consultationText: String {
        let actualBankRate = isBankRateMonthly ? bankRate * 12.0 : bankRate
        let actualTctdRate = isTctdRateMonthly ? tctdRate * 12.0 : tctdRate
        return DebtCalculator.generateDebtConsultationText(
            customerName: customerName,
            startDateStr: startDateStr,
            bankRate: actualBankRate,
            tctdRate: actualTctdRate,
            items: items,
            result: calculationResult
        )
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Row
                HStack {
                    HStack(spacing: 6) {
                        Image(systemName: "person.circle.fill")
                            .foregroundColor(AppTheme.orange)
                        TextField("Tên khách hàng", text: $customerName)
                            .font(.subheadline)
                    }
                    .padding(8)
                    .background(Color(UIColor.secondarySystemGroupedBackground))
                    .cornerRadius(10)
                    
                    Button {
                        showPresetsSheet = true
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "bookmark.fill")
                            Text("Mẫu")
                        }
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 8)
                        .background(AppTheme.orangeContainer)
                        .foregroundColor(AppTheme.orange)
                        .cornerRadius(10)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // Rates & Method Card
                VStack(spacing: 14) {
                    DateInputField(
                        label: "Mốc thời gian tổng hợp nợ",
                        dateStr: $startDateStr,
                        accentColor: AppTheme.orange
                    )
                    
                    RateInputField(
                        label: "Lãi suất khối Ngân hàng (chung)",
                        rate: $bankRate,
                        isMonthly: $isBankRateMonthly,
                        quickRates: [7.5, 8.5, 9.5, 10.5, 12.0],
                        accentColor: AppTheme.primary
                    )
                    
                    RateInputField(
                        label: "Lãi suất khối TCTD / Công ty TC",
                        rate: $tctdRate,
                        isMonthly: $isTctdRateMonthly,
                        quickRates: [18.0, 24.0, 28.0, 36.0, 45.0],
                        accentColor: AppTheme.orange
                    )
                    
                    Divider()
                    
                    // Method Selector
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Phương thức tính trả góp")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        Picker("", selection: $selectedMethod) {
                            Text("Dư nợ giảm dần").tag(LoanMethod.reducingBalance)
                            Text("Trả góp đều").tag(LoanMethod.annuity)
                            Text("Lãi phẳng").tag(LoanMethod.flatRate)
                            Text("Lãi cuối kỳ").tag(LoanMethod.endOfTerm)
                        }
                        .pickerStyle(.segmented)
                    }
                }
                .padding(16)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                .cornerRadius(18)
                .padding(.horizontal, 16)
                
                // Debt Sources List Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Danh sách các khoản nợ")
                                .font(.headline)
                                .fontWeight(.bold)
                            Text("\(items.count) khoản đang theo dõi")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        
                        Spacer()
                        
                        Button {
                            showAddItemSheet = true
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "plus")
                                Text("Thêm nợ")
                            }
                            .font(.caption)
                            .fontWeight(.bold)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(AppTheme.orange)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                        }
                    }
                    
                    Divider()
                    
                    if items.isEmpty {
                        Text("Chưa có khoản nợ nào. Nhấn 'Thêm nợ' để bắt đầu.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .padding(.vertical, 10)
                    } else {
                        VStack(spacing: 8) {
                            ForEach(items) { item in
                                HStack {
                                    VStack(alignment: .leading, spacing: 3) {
                                        HStack(spacing: 6) {
                                            Text(item.sourceType.shortTitle)
                                                .font(.system(size: 9, weight: .bold))
                                                .foregroundColor(.white)
                                                .padding(.horizontal, 5)
                                                .padding(.vertical, 2)
                                                .background(item.sourceType.color)
                                                .cornerRadius(4)
                                            Text(item.name.isEmpty ? item.sourceType.title : item.name)
                                                .font(.subheadline)
                                                .fontWeight(.semibold)
                                        }
                                        
                                        HStack(spacing: 8) {
                                            Text("Dư nợ: \(CurrencyUtils.formatCompactVnd(item.currentDebt))")
                                                .font(.caption)
                                                .foregroundColor(item.sourceType.color)
                                                .fontWeight(.bold)
                                            if !item.maturityDate.isEmpty {
                                                Text("Đáo hạn: \(item.maturityDate)")
                                                    .font(.caption2)
                                                    .foregroundColor(.secondary)
                                            }
                                        }
                                    }
                                    
                                    Spacer()
                                    
                                    Button {
                                        itemToEdit = item
                                    } label: {
                                        Image(systemName: "pencil")
                                            .font(.caption)
                                            .padding(6)
                                            .background(Color.gray.opacity(0.1))
                                            .foregroundColor(.primary)
                                            .clipShape(Circle())
                                    }
                                    
                                    Button {
                                        items.removeAll { $0.id == item.id }
                                        saveChanges()
                                    } label: {
                                        Image(systemName: "trash")
                                            .font(.caption)
                                            .padding(6)
                                            .background(Color.red.opacity(0.1))
                                            .foregroundColor(.red)
                                            .clipShape(Circle())
                                    }
                                }
                                .padding(10)
                                .background(Color(UIColor.systemBackground))
                                .cornerRadius(10)
                            }
                        }
                    }
                }
                .padding(16)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                .cornerRadius(18)
                .padding(.horizontal, 16)
                
                // Result Summary Card
                VStack(spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Tổng dư nợ các nguồn:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text(CurrencyUtils.formatVnd(calculationResult.totalInitialDebt))
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Lãi suất bình quân:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text("\(String(format: "%.2f", calculationResult.weightedAverageRate))%/năm")
                                .font(.system(size: 16, weight: .semibold, design: .rounded))
                                .foregroundColor(.white)
                        }
                    }
                    
                    Divider().overlay(Color.white.opacity(0.3))
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Trả tháng đầu:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.firstMonthPayment))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Tổng lãi cả chu kỳ:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalInterest))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }
                    
                    HStack {
                        Text("• Dư nợ NH: \(CurrencyUtils.formatCompactVnd(calculationResult.bankInitialDebt))")
                            .font(.caption2)
                            .foregroundColor(.white.opacity(0.9))
                        Spacer()
                        Text("• Dư nợ TCTD: \(CurrencyUtils.formatCompactVnd(calculationResult.tctdInitialDebt))")
                            .font(.caption2)
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                .padding(16)
                .background(AppTheme.orangeGradient)
                .cornerRadius(18)
                .shadow(color: AppTheme.orange.opacity(0.25), radius: 8, x: 0, y: 4)
                .padding(.horizontal, 16)
                
                // Action Buttons
                VStack(spacing: 10) {
                    HStack(spacing: 10) {
                        Button {
                            showScheduleSheet = true
                        } label: {
                            HStack {
                                Image(systemName: "tablecells")
                                Text("Lịch trả nợ")
                            }
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .foregroundColor(AppTheme.orange)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.orange.opacity(0.4), lineWidth: 1)
                            )
                        }
                        
                        Button {
                            showShareSheet = true
                        } label: {
                            HStack {
                                Image(systemName: "square.and.arrow.up")
                                Text("Bảng tư vấn")
                            }
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(AppTheme.orange)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .background(AppTheme.background)
        .onAppear {
            loadFromLastParams(repository.lastDebtParams)
        }
        .sheet(isPresented: $showScheduleSheet) {
            DebtScheduleSheet(
                customerName: customerName,
                result: calculationResult,
                onShare: { showShareSheet = true }
            )
        }
        .sheet(isPresented: $showShareSheet) {
            ShareQuoteSheet(
                title: "Bảng tư vấn gom nợ đa nguồn",
                quoteText: consultationText
            )
        }
        .sheet(isPresented: $showPresetsSheet) {
            SavedDebtParamsSheet(
                currentParams: currentAsParams(),
                presets: repository.savedDebtPresets,
                onLoad: { selected in
                    loadFromLastParams(selected)
                },
                onSaveNew: { newP in
                    repository.saveDebtPreset(newP)
                },
                onDelete: { id in
                    repository.deleteDebtPreset(id: id)
                }
            )
        }
        .sheet(item: $itemToEdit) { item in
            EditDebtItemSheet(item: item) { updated in
                if let idx = items.firstIndex(where: { $0.id == updated.id }) {
                    items[idx] = updated
                    saveChanges()
                }
            }
        }
        .sheet(isPresented: $showAddItemSheet) {
            EditDebtItemSheet(item: nil) { newItem in
                items.append(newItem)
                saveChanges()
            }
        }
    }
    
    private func saveChanges() {
        repository.updateLastDebtParams(currentAsParams())
    }
    
    private func currentAsParams() -> SavedDebtParams {
        SavedDebtParams(
            customerName: customerName,
            startDateStr: startDateStr,
            bankRate: bankRate,
            isBankRateMonthly: isBankRateMonthly,
            tctdRate: tctdRate,
            isTctdRateMonthly: isTctdRateMonthly,
            calculationMethod: selectedMethod,
            items: items
        )
    }
    
    private func loadFromLastParams(_ p: SavedDebtParams) {
        self.customerName = p.customerName
        self.startDateStr = p.startDateStr.isEmpty ? DateTimeUtils.todayString() : p.startDateStr
        self.bankRate = p.bankRate
        self.isBankRateMonthly = p.isBankRateMonthly
        self.tctdRate = p.tctdRate
        self.isTctdRateMonthly = p.isTctdRateMonthly
        self.selectedMethod = p.calculationMethod
        self.items = p.items
    }
}

private struct EditDebtItemSheet: View {
    @Environment(\.dismiss) private var dismiss
    let initialItem: DebtSourceItem?
    let onSave: (DebtSourceItem) -> Void
    
    @State private var name: String
    @State private var sourceType: DebtSourceType
    @State private var currentDebt: Double
    @State private var maturityDate: String
    @State private var customRateText: String
    
    init(item: DebtSourceItem?, onSave: @escaping (DebtSourceItem) -> Void) {
        self.onSave = onSave
        self.initialItem = item
        let i = item ?? DebtSourceItem(
            name: "",
            sourceType: .bank,
            currentDebt: 50_000_000.0,
            maturityDate: DateTimeUtils.addMonths(DateTimeUtils.todayString(), 12)
        )
        _name = State(initialValue: i.name)
        _sourceType = State(initialValue: i.sourceType)
        _currentDebt = State(initialValue: i.currentDebt)
        _maturityDate = State(initialValue: i.maturityDate)
        _customRateText = State(initialValue: i.customRate != nil ? String(format: "%.1f", i.customRate!) : "")
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin khoản nợ") {
                    TextField("Tên nguồn nợ (VD: Vietcombank, FE Credit, Thẻ tín dụng)", text: $name)
                    
                    Picker("Phân loại nguồn", selection: $sourceType) {
                        ForEach(DebtSourceType.allCases, id: \.self) { st in
                            Text(st.title).tag(st)
                        }
                    }
                }
                
                Section("Dư nợ & Thời hạn") {
                    MoneyInputField(
                        label: "Dư nợ hiện tại",
                        amount: $currentDebt,
                        accentColor: sourceType == .bank ? AppTheme.primary : AppTheme.orange
                    )
                    
                    DateInputField(
                        label: "Ngày đáo hạn hợp đồng",
                        dateStr: $maturityDate,
                        accentColor: sourceType == .bank ? AppTheme.primary : AppTheme.orange
                    )
                }
                
                Section("Lãi suất riêng (Tùy chọn)") {
                    TextField("Lãi riêng (%/năm, để trống sẽ dùng lãi chung)", text: $customRateText)
                        .keyboardType(.decimalPad)
                }
            }
            .navigationTitle(initialItem != nil ? "Sửa khoản nợ" : "Thêm khoản nợ mới")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        var updated = initialItem ?? DebtSourceItem()
                        updated.name = name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? sourceType.title : name
                        updated.sourceType = sourceType
                        updated.currentDebt = currentDebt
                        updated.maturityDate = maturityDate
                        let parsedRate = Double(customRateText.replacingOccurrences(of: ",", with: "."))
                        updated.customRate = (parsedRate ?? 0) > 0 ? parsedRate : nil
                        
                        onSave(updated)
                        dismiss()
                    }
                    .fontWeight(.bold)
                }
            }
        }
    }
}

