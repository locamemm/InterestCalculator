//
//  BankDataRepository.swift
//  InterestCalculator
//

import Foundation
import Combine

class BankDataRepository: ObservableObject {
    @Published var loans: [LoanProfile] = []
    @Published var savings: [SavingsProfile] = []
    @Published var kpi: KpiProfile = KpiProfile()
    
    @Published var lastLoanParams: SavedLoanParams = SavedLoanParams()
    @Published var lastSavingsParams: SavedSavingsParams = SavedSavingsParams()
    @Published var savedLoanPresets: [SavedLoanParams] = []
    @Published var savedSavingsPresets: [SavedSavingsParams] = []
    
    @Published var lastDebtParams: SavedDebtParams
    @Published var savedDebtPresets: [SavedDebtParams] = []
    
    private let storageUrl: URL
    
    init() {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        self.storageUrl = docs.appendingPathComponent("bank_records.json")
        
        let today = DateTimeUtils.todayString()
        self.lastDebtParams = SavedDebtParams(
            startDateStr: today,
            bankRate: 10.5,
            tctdRate: 28.0,
            calculationMethod: .reducingBalance,
            items: [
                DebtSourceItem(
                    name: "Vietcombank",
                    sourceType: .bank,
                    currentDebt: 300_000_000.0,
                    maturityDate: DateTimeUtils.addMonths(today, 24)
                ),
                DebtSourceItem(
                    name: "FE Credit",
                    sourceType: .creditInstitution,
                    currentDebt: 50_000_000.0,
                    maturityDate: DateTimeUtils.addMonths(today, 12)
                )
            ]
        )
        
        loadData()
    }
    
    private struct StorageContainer: Codable {
        var loans: [LoanProfile]
        var savings: [SavingsProfile]
        var kpi: KpiProfile
        var lastLoanParams: SavedLoanParams?
        var lastSavingsParams: SavedSavingsParams?
        var savedLoanPresets: [SavedLoanParams]?
        var savedSavingsPresets: [SavedSavingsParams]?
        var lastDebtParams: SavedDebtParams?
        var savedDebtPresets: [SavedDebtParams]?
    }
    
    func loadData() {
        guard FileManager.default.fileExists(atPath: storageUrl.path),
              let data = try? Data(contentsOf: storageUrl) else {
            initDefaultSampleData()
            saveDataToFile()
            return
        }
        
        do {
            let container = try JSONDecoder().decode(StorageContainer.self, from: data)
            self.loans = container.loans
            
            // Auto check expiring soon
            self.savings = container.savings.map { item in
                var mod = item
                if mod.status == .active && DateTimeUtils.isExpiringSoon(mod.maturityDate, thresholdDays: 15) {
                    mod.status = .expiringSoon
                }
                return mod
            }
            
            self.kpi = container.kpi
            if let lp = container.lastLoanParams { self.lastLoanParams = lp }
            if let sp = container.lastSavingsParams { self.lastSavingsParams = sp }
            if let lps = container.savedLoanPresets { self.savedLoanPresets = lps }
            if let sps = container.savedSavingsPresets { self.savedSavingsPresets = sps }
            if let dp = container.lastDebtParams { self.lastDebtParams = dp }
            if let dps = container.savedDebtPresets { self.savedDebtPresets = dps }
        } catch {
            print("Failed to decode bank data: \(error)")
            initDefaultSampleData()
            saveDataToFile()
        }
    }
    
    func saveDataToFile() {
        let container = StorageContainer(
            loans: loans,
            savings: savings,
            kpi: kpi,
            lastLoanParams: lastLoanParams,
            lastSavingsParams: lastSavingsParams,
            savedLoanPresets: savedLoanPresets,
            savedSavingsPresets: savedSavingsPresets,
            lastDebtParams: lastDebtParams,
            savedDebtPresets: savedDebtPresets
        )
        
        DispatchQueue.global(qos: .background).async { [weak self] in
            guard let self = self else { return }
            do {
                let data = try JSONEncoder().encode(container)
                try data.write(to: self.storageUrl, options: .atomic)
            } catch {
                print("Failed to save bank data: \(error)")
            }
        }
    }
    
    private func initDefaultSampleData() {
        let today = DateTimeUtils.todayString()
        
        self.loans = [
            LoanProfile(
                customerName: "Nguyễn Văn An",
                customerPhone: "0912345678",
                loanPurpose: "Vay mua nhà chung cư Masteri",
                principalAmount: 1_000_000_000.0,
                termMonths: 120,
                interestRate: 9.5,
                hasPreferential: true,
                preferentialMonths: 12,
                preferentialRate: 6.8,
                gracePeriodMonths: 6,
                calculationMethod: .reducingBalance,
                disbursementDate: today,
                status: .disbursed,
                note: "Đã phong tỏa tài sản đảm bảo, giải ngân đợt 1"
            ),
            LoanProfile(
                customerName: "Trần Thị Bích",
                customerPhone: "0987654321",
                loanPurpose: "Vay mua ô tô Hyundai Tucson",
                principalAmount: 600_000_000.0,
                termMonths: 60,
                interestRate: 10.2,
                hasPreferential: true,
                preferentialMonths: 6,
                preferentialRate: 7.5,
                calculationMethod: .annuity,
                disbursementDate: today,
                status: .approved,
                note: "Khách chọn nhận xe ngày 15"
            ),
            LoanProfile(
                customerName: "Công ty TNHH Hoàng Nam",
                customerPhone: "0903112233",
                loanPurpose: "Bổ sung vốn kinh doanh cuối năm",
                principalAmount: 1_500_000_000.0,
                termMonths: 12,
                interestRate: 8.5,
                hasPreferential: false,
                calculationMethod: .reducingBalance,
                disbursementDate: today,
                status: .consulting,
                note: "Đang thẩm định báo cáo tài chính quý 3"
            )
        ]
        
        self.savings = [
            SavingsProfile(
                customerName: "Bác Lê Thị Mai",
                customerPhone: "0934567890",
                depositAmount: 500_000_000.0,
                termMonths: 6,
                interestRate: 5.8,
                interestPaymentType: .endOfTerm,
                isAutoRenewCompounding: false,
                depositDate: DateTimeUtils.addDays(today, -170),
                maturityDate: DateTimeUtils.addDays(today, 10),
                status: .expiringSoon,
                note: "Tiết kiệm dưỡng già, hẹn gọi điện tư vấn tái tục gói 12 tháng"
            ),
            SavingsProfile(
                customerName: "Phạm Quang Dũng",
                customerPhone: "0971239988",
                depositAmount: 2_000_000_000.0,
                termMonths: 12,
                interestRate: 6.2,
                interestPaymentType: .monthly,
                isAutoRenewCompounding: false,
                depositDate: today,
                maturityDate: DateTimeUtils.addMonths(today, 12),
                status: .active,
                note: "Khách VIP, nhận lãi hàng tháng về tài khoản thanh toán"
            ),
            SavingsProfile(
                customerName: "Đỗ Thu Hà",
                customerPhone: "0918889900",
                depositAmount: 300_000_000.0,
                termMonths: 3,
                interestRate: 4.5,
                interestPaymentType: .endOfTerm,
                isAutoRenewCompounding: true,
                depositDate: today,
                maturityDate: DateTimeUtils.addMonths(today, 3),
                status: .active,
                note: "Mở sổ online, chế độ tự động quay vòng gốc và lãi"
            )
        ]
        
        self.kpi = KpiProfile(
            staffName: "Chuyên viên Tín dụng & Huy động",
            bankName: "Ngân hàng Thương mại",
            loanDisbursementTarget: 5_000_000_000.0,
            savingsMobilizationTarget: 10_000_000_000.0
        )
    }
    
    // MARK: - Loans
    func addLoan(_ loan: LoanProfile) {
        loans.insert(loan, at: 0)
        saveDataToFile()
    }
    
    func updateLoan(_ loan: LoanProfile) {
        if let idx = loans.firstIndex(where: { $0.id == loan.id }) {
            loans[idx] = loan
            saveDataToFile()
        }
    }
    
    func deleteLoan(id: String) {
        loans.removeAll { $0.id == id }
        saveDataToFile()
    }
    
    // MARK: - Savings
    func addSavings(_ saving: SavingsProfile) {
        savings.insert(saving, at: 0)
        saveDataToFile()
    }
    
    func updateSavings(_ saving: SavingsProfile) {
        if let idx = savings.firstIndex(where: { $0.id == saving.id }) {
            savings[idx] = saving
            saveDataToFile()
        }
    }
    
    func deleteSavings(id: String) {
        savings.removeAll { $0.id == id }
        saveDataToFile()
    }
    
    // MARK: - KPI
    func updateKpi(_ newKpi: KpiProfile) {
        self.kpi = newKpi
        saveDataToFile()
    }
    
    // MARK: - Loan Presets
    func updateLastLoanParams(_ params: SavedLoanParams) {
        self.lastLoanParams = params
        saveDataToFile()
    }
    
    func saveLoanPreset(_ params: SavedLoanParams) {
        savedLoanPresets.insert(params, at: 0)
        saveDataToFile()
    }
    
    func deleteLoanPreset(id: String) {
        savedLoanPresets.removeAll { $0.id == id }
        saveDataToFile()
    }
    
    // MARK: - Savings Presets
    func updateLastSavingsParams(_ params: SavedSavingsParams) {
        self.lastSavingsParams = params
        saveDataToFile()
    }
    
    func saveSavingsPreset(_ params: SavedSavingsParams) {
        savedSavingsPresets.insert(params, at: 0)
        saveDataToFile()
    }
    
    func deleteSavingsPreset(id: String) {
        savedSavingsPresets.removeAll { $0.id == id }
        saveDataToFile()
    }
    
    // MARK: - Debt Presets
    func updateLastDebtParams(_ params: SavedDebtParams) {
        self.lastDebtParams = params
        saveDataToFile()
    }
    
    func saveDebtPreset(_ params: SavedDebtParams) {
        savedDebtPresets.insert(params, at: 0)
        saveDataToFile()
    }
    
    func deleteDebtPreset(id: String) {
        savedDebtPresets.removeAll { $0.id == id }
        saveDataToFile()
    }
}

