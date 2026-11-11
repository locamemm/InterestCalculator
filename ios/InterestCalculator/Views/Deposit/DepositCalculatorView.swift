//
//  DepositCalculatorView.swift
//  InterestCalculator
//

import SwiftUI

struct DepositCalculatorView: View {
    @ObservedObject var repository: BankDataRepository
    
    @State private var customerName: String = ""
    @State private var startDateStr: String = DateTimeUtils.todayString()
    @State private var counterRate: Double = 5.5
    @State private var isCounterRateMonthly: Bool = false
    @State private var onlineRate: Double = 6.0
    @State private var isOnlineRateMonthly: Bool = false
    @State private var paymentType: SavingsPaymentType = .endOfTerm
    @State private var isCompounding: Bool = false
    @State private var items: [DepositSourceItem] = []
    
    // Sheets
    @State private var showScheduleSheet: Bool = false
    @State private var showShareSheet: Bool = false
    @State private var showPresetsSheet: Bool = false
    @State private var itemToEdit: DepositSourceItem? = nil
    @State private var showAddItemSheet: Bool = false
    
    private var calculationResult: DepositScheduleResult {
        let actualCounterRate = isCounterRateMonthly ? counterRate * 12.0 : counterRate
        let actualOnlineRate = isOnlineRateMonthly ? onlineRate * 12.0 : onlineRate
        
        return DepositCalculator.calculate(
            items: items,
            startDateStr: startDateStr,
            counterRate: actualCounterRate,
            onlineRate: actualOnlineRate,
            paymentType: paymentType,
            isCompounding: isCompounding
        )
    }
    
    private var consultationText: String {
        let actualCounterRate = isCounterRateMonthly ? counterRate * 12.0 : counterRate
        let actualOnlineRate = isOnlineRateMonthly ? onlineRate * 12.0 : onlineRate
        return DepositCalculator.generateDepositConsultationText(
            customerName: customerName,
            startDateStr: startDateStr,
            counterRate: actualCounterRate,
            onlineRate: actualOnlineRate,
            paymentType: paymentType,
            isCompounding: isCompounding,
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
                            .foregroundColor(AppTheme.teal)
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
                        .background(AppTheme.tealContainer)
                        .foregroundColor(AppTheme.teal)
                        .cornerRadius(10)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // Rates & Payment Type Card
                VStack(spacing: 14) {
                    DateInputField(
                        label: "Mốc thời gian bắt đầu tính",
                        dateStr: $startDateStr,
                        accentColor: AppTheme.teal
                    )
                    
                    RateInputField(
                        label: "Lãi suất Gửi tại quầy (chung)",
                        rate: $counterRate,
                        isMonthly: $isCounterRateMonthly,
                        quickRates: [4.5, 5.0, 5.5, 6.0, 6.5],
                        accentColor: AppTheme.primary
                    )
                    
                    RateInputField(
                        label: "Lãi suất Gửi Online (chung)",
                        rate: $onlineRate,
                        isMonthly: $isOnlineRateMonthly,
                        quickRates: [5.0, 5.5, 6.0, 6.5, 7.0],
                        accentColor: AppTheme.green
                    )
                    
                    Divider()
                    
                    // Payment Type Selector
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Hình thức nhận lãi")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        Picker("", selection: $paymentType) {
                            ForEach(SavingsPaymentType.allCases, id: \.self) { pt in
                                Text(pt.shortTitle).tag(pt)
                            }
                        }
                        .pickerStyle(.segmented)
                    }
                    
                    if paymentType == .endOfTerm {
                        Toggle(isOn: $isCompounding) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Lãi nhập gốc hàng tháng (lãi kép)")
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                Text("Tiền lãi mỗi tháng tự động cộng dồn vào tiền gốc")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .tint(AppTheme.teal)
                    }
                }
                .padding(16)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                .cornerRadius(18)
                .padding(.horizontal, 16)
                
                // Deposit Sources List Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Danh sách sổ / nguồn tiền gửi")
                                .font(.headline)
                                .fontWeight(.bold)
                            Text("\(items.count) sổ đang theo dõi")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        
                        Spacer()
                        
                        Button {
                            showAddItemSheet = true
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "plus")
                                Text("Thêm sổ")
                            }
                            .font(.caption)
                            .fontWeight(.bold)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(AppTheme.teal)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                        }
                    }
                    
                    Divider()
                    
                    if items.isEmpty {
                        Text("Chưa có sổ tiền gửi nào. Nhấn 'Thêm sổ' để bắt đầu.")
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
                                            Text("Tiền gửi: \(CurrencyUtils.formatCompactVnd(item.principalAmount))")
                                                .font(.caption)
                                                .foregroundColor(item.sourceType.color)
                                                .fontWeight(.bold)
                                            if !item.maturityDate.isEmpty {
                                                Text("Đáo hạn: \(item.maturityDate)")
                                                    .font(.caption2)
                                                    .foregroundColor(.secondary)
                                            }
                                        }
                                        if let cr = item.customRate, cr > 0 {
                                            Text("Lãi riêng: \(String(format: "%.2f", cr))%/năm")
                                                .font(.caption2)
                                                .foregroundColor(AppTheme.teal)
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
                            Text("Tổng gốc & lãi dự kiến nhận:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text(CurrencyUtils.formatVnd(calculationResult.totalMaturityAmount))
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
                            Text("Tổng tiền gốc:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalPrincipal))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Tổng tiền lãi:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalInterest))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }
                    
                    HStack {
                        Text("• Tại quầy: \(CurrencyUtils.formatCompactVnd(calculationResult.counterPrincipal))")
                            .font(.caption2)
                            .foregroundColor(.white.opacity(0.9))
                        Spacer()
                        Text("• Online: \(CurrencyUtils.formatCompactVnd(calculationResult.onlinePrincipal))")
                            .font(.caption2)
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                .padding(16)
                .background(AppTheme.tealGradient)
                .cornerRadius(18)
                .shadow(color: AppTheme.teal.opacity(0.25), radius: 8, x: 0, y: 4)
                .padding(.horizontal, 16)
                
                // Action Buttons
                VStack(spacing: 10) {
                    HStack(spacing: 10) {
                        Button {
                            showScheduleSheet = true
                        } label: {
                            HStack {
                                Image(systemName: "tablecells")
                                Text("Lịch nhận lãi")
                            }
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .foregroundColor(AppTheme.teal)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.teal.opacity(0.4), lineWidth: 1)
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
                            .background(AppTheme.teal)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .background(AppTheme.background)
        .onAppear {
            loadFromLastParams(repository.lastDepositParams)
        }
        .sheet(isPresented: $showScheduleSheet) {
            DepositScheduleSheet(
                customerName: customerName,
                result: calculationResult,
                onShare: { showShareSheet = true }
            )
        }
        .sheet(isPresented: $showShareSheet) {
            ShareQuoteSheet(
                title: "Bảng dự tính lãi tiền gửi đa nguồn",
                quoteText: consultationText
            )
        }
        .sheet(isPresented: $showPresetsSheet) {
            SavedDepositParamsSheet(
                currentParams: currentAsParams(),
                presets: repository.savedDepositPresets,
                onLoad: { selected in
                    loadFromLastParams(selected)
                },
                onSaveNew: { newP in
                    repository.saveDepositPreset(newP)
                },
                onDelete: { id in
                    repository.deleteDepositPreset(id: id)
                }
            )
        }
        .sheet(item: $itemToEdit) { item in
            EditDepositItemSheet(item: item) { updated in
                if let idx = items.firstIndex(where: { $0.id == updated.id }) {
                    items[idx] = updated
                    saveChanges()
                }
            }
        }
        .sheet(isPresented: $showAddItemSheet) {
            EditDepositItemSheet(item: nil) { newItem in
                items.append(newItem)
                saveChanges()
            }
        }
    }
    
    private func saveChanges() {
        repository.updateLastDepositParams(currentAsParams())
    }
    
    private func currentAsParams() -> SavedDepositParams {
        SavedDepositParams(
            customerName: customerName,
            startDateStr: startDateStr,
            counterRate: counterRate,
            isCounterRateMonthly: isCounterRateMonthly,
            onlineRate: onlineRate,
            isOnlineRateMonthly: isOnlineRateMonthly,
            paymentType: paymentType,
            isCompounding: isCompounding,
            items: items
        )
    }
    
    private func loadFromLastParams(_ p: SavedDepositParams) {
        self.customerName = p.customerName
        self.startDateStr = p.startDateStr.isEmpty ? DateTimeUtils.todayString() : p.startDateStr
        self.counterRate = p.counterRate
        self.isCounterRateMonthly = p.isCounterRateMonthly
        self.onlineRate = p.onlineRate
        self.isOnlineRateMonthly = p.isOnlineRateMonthly
        self.paymentType = p.paymentType
        self.isCompounding = p.isCompounding
        self.items = p.items
    }
}

private struct EditDepositItemSheet: View {
    @Environment(\.dismiss) private var dismiss
    let initialItem: DepositSourceItem?
    let onSave: (DepositSourceItem) -> Void
    
    @State private var name: String
    @State private var sourceType: DepositSourceType
    @State private var principalAmount: Double
    @State private var maturityDate: String
    @State private var customRateText: String
    
    init(item: DepositSourceItem?, onSave: @escaping (DepositSourceItem) -> Void) {
        self.onSave = onSave
        self.initialItem = item
        let i = item ?? DepositSourceItem(
            name: "",
            sourceType: .counter,
            principalAmount: 100_000_000.0,
            maturityDate: DateTimeUtils.addMonths(DateTimeUtils.todayString(), 12)
        )
        _name = State(initialValue: i.name)
        _sourceType = State(initialValue: i.sourceType)
        _principalAmount = State(initialValue: i.principalAmount)
        _maturityDate = State(initialValue: i.maturityDate)
        _customRateText = State(initialValue: i.customRate != nil ? String(format: "%.1f", i.customRate!) : "")
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Thông tin sổ tiền gửi") {
                    TextField("Tên ngân hàng / sổ (VD: Vietcombank, Techcombank)", text: $name)
                    
                    Picker("Kênh gửi tiền", selection: $sourceType) {
                        ForEach(DepositSourceType.allCases, id: \.self) { st in
                            Text(st.title).tag(st)
                        }
                    }
                }
                
                Section("Số tiền & Ngày đáo hạn") {
                    MoneyInputField(
                        label: "Số tiền gửi gốc",
                        amount: $principalAmount,
                        accentColor: sourceType == .counter ? AppTheme.primary : AppTheme.green
                    )
                    
                    DateInputField(
                        label: "Ngày đáo hạn dự kiến",
                        dateStr: $maturityDate,
                        accentColor: sourceType == .counter ? AppTheme.primary : AppTheme.green
                    )
                }
                
                Section("Lãi suất riêng (Tùy chọn)") {
                    TextField("Lãi riêng (%/năm, để trống sẽ dùng lãi chung)", text: $customRateText)
                        .keyboardType(.decimalPad)
                }
            }
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(initialItem != nil ? "Sửa sổ tiền gửi" : "Thêm sổ tiền gửi")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Hủy") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Lưu") {
                        var updated = initialItem ?? DepositSourceItem()
                        updated.name = name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? sourceType.title : name
                        updated.sourceType = sourceType
                        updated.principalAmount = principalAmount
                        updated.maturityDate = maturityDate
                        let parsedRate = Double(customRateText.replacingOccurrences(of: ",", with: "."))
                        updated.customRate = (parsedRate ?? 0) > 0 ? parsedRate : nil
                        
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

