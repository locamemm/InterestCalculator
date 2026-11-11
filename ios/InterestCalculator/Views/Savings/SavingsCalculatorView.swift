//
//  SavingsCalculatorView.swift
//  InterestCalculator
//

import SwiftUI

struct SavingsCalculatorView: View {
    @ObservedObject var repository: BankDataRepository
    var onSaveSavings: (SavingsProfile) -> Void
    
    @State private var customerName: String = ""
    @State private var depositAmount: Double = 500_000_000.0
    @State private var termMonths: Int = 12
    @State private var periodMonths: Int = 6
    @State private var interestRate: Double = 5.8
    @State private var startDateStr: String = DateTimeUtils.todayString()
    @State private var paymentType: SavingsPaymentType = .endOfTerm
    @State private var isCompounding: Bool = false
    
    // Sheets
    @State private var showScheduleSheet: Bool = false
    @State private var showShareSheet: Bool = false
    @State private var showSaveProfileSheet: Bool = false
    @State private var showPresetsSheet: Bool = false
    
    private var calculationResult: SavingsCalculationResult {
        SavingsCalculator.calculate(
            depositAmount: depositAmount,
            termMonths: max(1, termMonths),
            annualInterestRate: interestRate,
            paymentType: paymentType,
            isAutoRenewCompounding: isCompounding,
            startDateStr: startDateStr,
            periodMonths: periodMonths
        )
    }
    
    private var consultationText: String {
        SavingsCalculator.generateConsultationText(
            customerName: customerName,
            depositAmount: depositAmount,
            termMonths: termMonths,
            interestRate: interestRate,
            paymentType: paymentType,
            isAutoRenewCompounding: isCompounding,
            result: calculationResult,
            startDateStr: startDateStr,
            periodMonths: periodMonths
        )
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Action Bar
                HStack {
                    HStack(spacing: 6) {
                        Image(systemName: "person.circle.fill")
                            .foregroundColor(AppTheme.green)
                        TextField("Tên khách hàng (tùy chọn)", text: $customerName)
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
                        .background(AppTheme.greenContainer)
                        .foregroundColor(AppTheme.green)
                        .cornerRadius(10)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // Form Card
                VStack(spacing: 16) {
                    MoneyInputField(
                        label: "Số tiền gửi tiết kiệm",
                        amount: $depositAmount,
                        accentColor: AppTheme.green
                    )
                    
                    TermInputField(
                        label: "Thời hạn gửi",
                        termMonths: $termMonths,
                        quickTerms: [
                            ("1 tháng", 1),
                            ("3 tháng", 3),
                            ("6 tháng", 6),
                            ("9 tháng", 9),
                            ("12 tháng", 12),
                            ("18 tháng", 18),
                            ("24 tháng", 24),
                            ("36 tháng", 36)
                        ],
                        accentColor: AppTheme.green
                    )
                    
                    // Period Months
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Kỳ hạn quay vòng lãi")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        Picker("", selection: $periodMonths) {
                            Text("Bằng thời hạn").tag(termMonths)
                            Text("1 tháng").tag(1)
                            Text("3 tháng").tag(3)
                            Text("6 tháng").tag(6)
                            Text("12 tháng").tag(12)
                        }
                        .pickerStyle(.segmented)
                    }
                    
                    RateInputField(
                        label: "Lãi suất huy động",
                        rate: $interestRate,
                        isMonthly: .constant(false),
                        allowPeriodToggle: false,
                        quickRates: [4.0, 4.8, 5.2, 5.8, 6.5],
                        accentColor: AppTheme.green
                    )
                    
                    DateInputField(
                        label: "Ngày mở sổ / gửi tiền",
                        dateStr: $startDateStr,
                        accentColor: AppTheme.green
                    )
                    
                    Divider()
                    
                    // Payment Method Chips
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Hình thức nhận lãi")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        ForEach(SavingsPaymentType.allCases, id: \.self) { pt in
                            Button {
                                paymentType = pt
                            } label: {
                                HStack {
                                    Image(systemName: paymentType == pt ? "largecircle.fill.circle" : "circle")
                                        .foregroundColor(paymentType == pt ? AppTheme.green : .secondary)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(pt.title)
                                            .font(.subheadline)
                                            .fontWeight(paymentType == pt ? .bold : .medium)
                                            .foregroundColor(.primary)
                                        Text(pt.description)
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                    Spacer()
                                }
                                .padding(10)
                                .background(paymentType == pt ? AppTheme.green.opacity(0.08) : Color.clear)
                                .cornerRadius(10)
                            }
                        }
                    }
                    
                    // Auto-renew compounding toggle
                    if paymentType == .endOfTerm {
                        Divider()
                        Toggle(isOn: $isCompounding) {
                            VStack(alignment: .leading) {
                                Text("Lãi kép (Tự động nhập gốc)")
                                    .fontWeight(.medium)
                                Text("Tiền lãi mỗi kỳ được cộng vào gốc để tính lãi kỳ tiếp theo")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .tint(AppTheme.green)
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
                            Text("Tổng tiền lãi thực nhận:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text(CurrencyUtils.formatVnd(calculationResult.totalInterest))
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Lãi suất thực hưởng:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text("\(String(format: "%.2f", calculationResult.effectiveAnnualRate))%/năm")
                                .font(.system(size: 16, weight: .semibold, design: .rounded))
                                .foregroundColor(.white)
                        }
                    }
                    
                    Divider().overlay(Color.white.opacity(0.3))
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tiền gốc ban đầu:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(depositAmount))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Tổng nhận khi đáo hạn:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalMaturityAmount))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }
                }
                .padding(16)
                .background(AppTheme.greenGradient)
                .cornerRadius(18)
                .shadow(color: AppTheme.green.opacity(0.25), radius: 8, x: 0, y: 4)
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
                            .foregroundColor(AppTheme.green)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.green.opacity(0.4), lineWidth: 1)
                            )
                        }
                        
                        Button {
                            showShareSheet = true
                        } label: {
                            HStack {
                                Image(systemName: "square.and.arrow.up")
                                Text("Tư vấn")
                            }
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .foregroundColor(AppTheme.primary)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.primary.opacity(0.4), lineWidth: 1)
                            )
                        }
                    }
                    
                    Button {
                        showSaveProfileSheet = true
                    } label: {
                        HStack {
                            Image(systemName: "tray.and.arrow.down.fill")
                            Text("Lưu vào danh sách sổ tiết kiệm")
                        }
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(AppTheme.green)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .background(AppTheme.background)
        .onAppear {
            loadFromLastParams(repository.lastSavingsParams)
        }
        .sheet(isPresented: $showScheduleSheet) {
            SavingsScheduleSheet(
                title: "Lịch nhận lãi - \(customerName.isEmpty ? "Khách hàng" : customerName)",
                customerName: customerName,
                result: calculationResult,
                onShare: { showShareSheet = true }
            )
        }
        .sheet(isPresented: $showShareSheet) {
            ShareQuoteSheet(
                title: "Bảng tư vấn gửi tiết kiệm",
                quoteText: consultationText
            )
        }
        .sheet(isPresented: $showSaveProfileSheet) {
            SaveSavingsSheet(
                profile: nil,
                defaultSavings: SavingsProfile(
                    customerName: customerName,
                    depositAmount: depositAmount,
                    termMonths: termMonths,
                    periodMonths: periodMonths,
                    interestRate: interestRate,
                    interestPaymentType: paymentType,
                    isAutoRenewCompounding: isCompounding,
                    depositDate: startDateStr,
                    maturityDate: DateTimeUtils.addMonths(startDateStr, termMonths),
                    status: .active
                ),
                onSave: { newSavings in
                    repository.addSavings(newSavings)
                }
            )
        }
        .sheet(isPresented: $showPresetsSheet) {
            SavedSavingsParamsSheet(
                currentParams: currentAsParams(),
                presets: repository.savedSavingsPresets,
                onLoad: { selected in
                    loadFromLastParams(selected)
                },
                onSaveNew: { newP in
                    repository.saveSavingsPreset(newP)
                },
                onDelete: { id in
                    repository.deleteSavingsPreset(id: id)
                }
            )
        }
    }
    
    private func currentAsParams() -> SavedSavingsParams {
        SavedSavingsParams(
            customerName: customerName,
            depositAmount: depositAmount,
            termMonths: termMonths,
            periodMonths: periodMonths,
            interestRate: interestRate,
            startDateStr: startDateStr,
            paymentType: paymentType,
            isCompounding: isCompounding
        )
    }
    
    private func loadFromLastParams(_ p: SavedSavingsParams) {
        self.customerName = p.customerName
        self.depositAmount = p.depositAmount
        self.termMonths = p.termMonths
        self.periodMonths = p.periodMonths
        self.interestRate = p.interestRate
        self.startDateStr = p.startDateStr.isEmpty ? DateTimeUtils.todayString() : p.startDateStr
        self.paymentType = p.paymentType
        self.isCompounding = p.isCompounding
    }
}

