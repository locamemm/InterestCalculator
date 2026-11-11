//
//  LoanCalculatorView.swift
//  InterestCalculator
//

import SwiftUI

struct LoanCalculatorView: View {
    @ObservedObject var repository: BankDataRepository
    var onSaveLoan: (LoanProfile) -> Void
    
    @State private var customerName: String = ""
    @State private var principalAmount: Double = 1_000_000_000.0
    @State private var termMonths: Int = 120
    @State private var repaymentPeriodMonths: Int = 1
    @State private var baseRate: Double = 9.5
    @State private var isBaseRateMonthly: Bool = false
    @State private var startDateStr: String = DateTimeUtils.todayString()
    
    // Preferential
    @State private var hasPreferential: Bool = true
    @State private var preferentialMonths: Int = 12
    @State private var preferentialRate: Double = 6.8
    
    // Grace
    @State private var hasGracePeriod: Bool = false
    @State private var gracePeriodMonths: Int = 6
    
    // Method
    @State private var selectedMethod: LoanMethod = .reducingBalance
    
    // Sheets
    @State private var showScheduleSheet: Bool = false
    @State private var showShareSheet: Bool = false
    @State private var showSaveProfileSheet: Bool = false
    @State private var showPresetsSheet: Bool = false
    
    // Computed result
    private var calculationResult: LoanCalculationResult {
        let actualAnnualRate = isBaseRateMonthly ? baseRate * 12.0 : baseRate
        let actualPrefRate = hasPreferential ? preferentialRate : actualAnnualRate
        let actualGraceMonths = hasGracePeriod ? gracePeriodMonths : 0
        
        return LoanCalculator.calculate(
            principalAmount: principalAmount,
            termMonths: max(1, termMonths),
            baseInterestRate: actualAnnualRate,
            hasPreferential: hasPreferential,
            preferentialMonths: preferentialMonths,
            preferentialRate: actualPrefRate,
            gracePeriodMonths: actualGraceMonths,
            method: selectedMethod,
            startDateStr: startDateStr,
            repaymentPeriodMonths: repaymentPeriodMonths
        )
    }
    
    private var consultationText: String {
        let actualAnnualRate = isBaseRateMonthly ? baseRate * 12.0 : baseRate
        return LoanCalculator.generateConsultationText(
            customerName: customerName,
            principalAmount: principalAmount,
            termMonths: termMonths,
            baseRate: actualAnnualRate,
            hasPreferential: hasPreferential,
            preferentialMonths: preferentialMonths,
            preferentialRate: preferentialRate,
            method: selectedMethod,
            result: calculationResult,
            startDateStr: startDateStr,
            repaymentPeriodMonths: repaymentPeriodMonths
        )
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Action Bar: Presets & Quick Stats
                HStack {
                    HStack(spacing: 6) {
                        Image(systemName: "person.circle.fill")
                            .foregroundColor(AppTheme.primary)
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
                        .background(AppTheme.primaryContainer)
                        .foregroundColor(AppTheme.primary)
                        .cornerRadius(10)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // Form Card
                VStack(spacing: 16) {
                    MoneyInputField(
                        label: "Số tiền vay",
                        amount: $principalAmount,
                        accentColor: AppTheme.primary
                    )
                    
                    TermInputField(
                        label: "Thời hạn vay",
                        termMonths: $termMonths,
                        accentColor: AppTheme.primary
                    )
                    
                    // Repayment Period
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Kỳ hạn trả nợ")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        Picker("", selection: $repaymentPeriodMonths) {
                            Text("Hàng tháng").tag(1)
                            Text("3 tháng/kỳ").tag(3)
                            Text("6 tháng/kỳ").tag(6)
                            Text("12 tháng/kỳ").tag(12)
                        }
                        .pickerStyle(.segmented)
                    }
                    
                    RateInputField(
                        label: "Lãi suất vay",
                        rate: $baseRate,
                        isMonthly: $isBaseRateMonthly,
                        accentColor: AppTheme.primary
                    )
                    
                    DateInputField(
                        label: "Ngày giải ngân dự kiến",
                        dateStr: $startDateStr,
                        accentColor: AppTheme.primary
                    )
                    
                    Divider()
                    
                    // Preferential Rate Toggle
                    VStack(spacing: 10) {
                        Toggle(isOn: $hasPreferential) {
                            VStack(alignment: .leading) {
                                Text("Lãi suất ưu đãi cố định")
                                    .fontWeight(.medium)
                                Text("Áp dụng trong thời gian đầu gói vay")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .tint(AppTheme.primary)
                        
                        if hasPreferential {
                            HStack(spacing: 12) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Số tháng: \(preferentialMonths) th")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    Stepper("", value: $preferentialMonths, in: 1...max(1, termMonths))
                                        .labelsHidden()
                                }
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Lãi suất (%/năm)")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    TextField("0.0", value: $preferentialRate, format: .number)
                                        .keyboardType(.decimalPad)
                                        .font(.headline)
                                        .padding(8)
                                        .background(Color(UIColor.secondarySystemGroupedBackground))
                                        .cornerRadius(8)
                                }
                            }
                        }
                    }
                    
                    // Grace Period Toggle
                    VStack(spacing: 10) {
                        Toggle(isOn: $hasGracePeriod) {
                            VStack(alignment: .leading) {
                                Text("Ân hạn gốc (Chỉ trả lãi)")
                                    .fontWeight(.medium)
                                Text("Không phải trả tiền gốc những tháng đầu")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .tint(AppTheme.primary)
                        
                        if hasGracePeriod {
                            HStack {
                                Text("Thời gian ân hạn: \(gracePeriodMonths) tháng")
                                    .font(.subheadline)
                                Spacer()
                                Stepper("", value: $gracePeriodMonths, in: 1...max(1, termMonths - 1))
                                    .labelsHidden()
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Method Selector Chips
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Phương thức tính lãi")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.secondary)
                        
                        ForEach(LoanMethod.allCases, id: \.self) { m in
                            Button {
                                selectedMethod = m
                            } label: {
                                HStack {
                                    Image(systemName: selectedMethod == m ? "largecircle.fill.circle" : "circle")
                                        .foregroundColor(selectedMethod == m ? AppTheme.primary : .secondary)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(m.title)
                                            .font(.subheadline)
                                            .fontWeight(selectedMethod == m ? .bold : .medium)
                                            .foregroundColor(.primary)
                                        Text(m.description)
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                    Spacer()
                                }
                                .padding(10)
                                .background(selectedMethod == m ? AppTheme.primary.opacity(0.08) : Color.clear)
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
                            Text("Số tiền trả kỳ đầu:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text(CurrencyUtils.formatVnd(calculationResult.firstMonthPayment))
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Kỳ cao nhất:")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.85))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.maxMonthPayment))
                                .font(.system(size: 16, weight: .semibold, design: .rounded))
                                .foregroundColor(.white)
                        }
                    }
                    
                    Divider().overlay(Color.white.opacity(0.3))
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tổng tiền lãi:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalInterest))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Tổng gốc + lãi:")
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.8))
                            Text(CurrencyUtils.formatCompactVnd(calculationResult.totalPayment))
                                .font(.subheadline)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                    }
                }
                .padding(16)
                .background(AppTheme.primaryGradient)
                .cornerRadius(18)
                .shadow(color: AppTheme.primary.opacity(0.25), radius: 8, x: 0, y: 4)
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
                            .foregroundColor(AppTheme.primary)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.primary.opacity(0.4), lineWidth: 1)
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
                            .foregroundColor(AppTheme.green)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(AppTheme.green.opacity(0.4), lineWidth: 1)
                            )
                        }
                    }
                    
                    Button {
                        showSaveProfileSheet = true
                    } label: {
                        HStack {
                            Image(systemName: "person.crop.circle.badge.plus")
                            Text("Lưu vào hồ sơ khách hàng")
                        }
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(AppTheme.primary)
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
            loadFromLastParams(repository.lastLoanParams)
        }
        .sheet(isPresented: $showScheduleSheet) {
            LoanScheduleSheet(
                title: "Lịch trả nợ - \(customerName.isEmpty ? "Khách hàng" : customerName)",
                customerName: customerName,
                result: calculationResult,
                onShare: { showShareSheet = true }
            )
        }
        .sheet(isPresented: $showShareSheet) {
            ShareQuoteSheet(
                title: "Bảng tư vấn khoản vay",
                quoteText: consultationText
            )
        }
        .sheet(isPresented: $showSaveProfileSheet) {
            SaveLoanSheet(
                profile: nil,
                defaultLoan: LoanProfile(
                    customerName: customerName,
                    principalAmount: principalAmount,
                    termMonths: termMonths,
                    repaymentPeriodMonths: repaymentPeriodMonths,
                    interestRate: isBaseRateMonthly ? baseRate * 12.0 : baseRate,
                    hasPreferential: hasPreferential,
                    preferentialMonths: preferentialMonths,
                    preferentialRate: preferentialRate,
                    gracePeriodMonths: hasGracePeriod ? gracePeriodMonths : 0,
                    calculationMethod: selectedMethod,
                    disbursementDate: startDateStr,
                    status: .consulting
                ),
                onSave: { newLoan in
                    repository.addLoan(newLoan)
                }
            )
        }
        .sheet(isPresented: $showPresetsSheet) {
            SavedLoanParamsSheet(
                currentParams: currentAsParams(),
                presets: repository.savedLoanPresets,
                onLoad: { selected in
                    loadFromLastParams(selected)
                },
                onSaveNew: { newP in
                    repository.saveLoanPreset(newP)
                },
                onDelete: { id in
                    repository.deleteLoanPreset(id: id)
                }
            )
        }
    }
    
    private func currentAsParams() -> SavedLoanParams {
        SavedLoanParams(
            customerName: customerName,
            principalAmount: principalAmount,
            termMonths: termMonths,
            repaymentPeriodMonths: repaymentPeriodMonths,
            baseRate: baseRate,
            isBaseRateMonthly: isBaseRateMonthly,
            startDateStr: startDateStr,
            hasPreferential: hasPreferential,
            preferentialMonths: preferentialMonths,
            preferentialRate: preferentialRate,
            hasGracePeriod: hasGracePeriod,
            gracePeriodMonths: gracePeriodMonths,
            selectedMethod: selectedMethod
        )
    }
    
    private func loadFromLastParams(_ p: SavedLoanParams) {
        self.customerName = p.customerName
        self.principalAmount = p.principalAmount
        self.termMonths = p.termMonths
        self.repaymentPeriodMonths = p.repaymentPeriodMonths
        self.baseRate = p.baseRate
        self.isBaseRateMonthly = p.isBaseRateMonthly
        self.startDateStr = p.startDateStr.isEmpty ? DateTimeUtils.todayString() : p.startDateStr
        self.hasPreferential = p.hasPreferential
        self.preferentialMonths = p.preferentialMonths
        self.preferentialRate = p.preferentialRate
        self.hasGracePeriod = p.hasGracePeriod
        self.gracePeriodMonths = p.gracePeriodMonths
        self.selectedMethod = p.selectedMethod
    }
}

