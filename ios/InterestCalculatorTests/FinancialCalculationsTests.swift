//
//  FinancialCalculationsTests.swift
//  InterestCalculatorTests
//

import XCTest
@testable import InterestCalculator

final class FinancialCalculationsTests: XCTestCase {

    func testLoanReducingBalanceStandard() {
        // Khoản vay 120.000.000 ₫ trong 12 tháng, lãi suất 12%/năm
        // Gốc mỗi tháng = 10.000.000 ₫
        // Tháng 1: Dư nợ 120tr, Lãi = 120tr * 1% = 1.200.000 ₫ -> Tổng trả tháng 1 = 11.200.000 ₫
        // Tháng 12: Dư nợ 10tr, Lãi = 10tr * 1% = 100.000 ₫ -> Tổng trả tháng 12 = 10.100.000 ₫
        let result = LoanCalculator.calculate(
            principalAmount: 120_000_000.0,
            termMonths: 12,
            baseInterestRate: 12.0,
            method: .reducingBalance,
            startDateStr: "01/01/2026"
        )

        XCTAssertEqual(result.schedule.count, 12)
        XCTAssertEqual(result.totalPrincipal, 120_000_000.0, accuracy: 0.01)
        XCTAssertEqual(result.firstMonthPayment, 11_200_000.0, accuracy: 1.0)
        XCTAssertEqual(result.lastMonthPayment, 10_100_000.0, accuracy: 1.0)
        // Tổng lãi của 120tr qua 12 tháng: 120 * (12+1)/2 * 10tr * 0.01 = 7.800.000 ₫
        XCTAssertEqual(result.totalInterest, 7_800_000.0, accuracy: 10.0)
        XCTAssertEqual(result.schedule.last?.remainingBalance ?? -1, 0.0, accuracy: 0.01)
        XCTAssertEqual(result.schedule.first?.paymentDate, "01/02/2026")
        XCTAssertEqual(result.schedule.last?.paymentDate, "01/01/2027")
    }

    func testStartDateScheduleCalculation() {
        let startDate = "15/03/2026"
        let loanResult = LoanCalculator.calculate(
            principalAmount: 60_000_000.0,
            termMonths: 6,
            baseInterestRate: 12.0,
            startDateStr: startDate
        )
        XCTAssertEqual(loanResult.schedule[0].paymentDate, "15/04/2026")
        XCTAssertEqual(loanResult.schedule.last?.paymentDate, "15/09/2026")

        let savingsResult = SavingsCalculator.calculate(
            depositAmount: 50_000_000.0,
            termMonths: 6,
            annualInterestRate: 6.0,
            startDateStr: startDate
        )
        XCTAssertEqual(savingsResult.schedule.first?.payoutDate, "15/09/2026")
    }

    func testLoanWithGracePeriodAndPreferentialRate() {
        // Vay 100tr, 12 tháng. 3 tháng đầu ân hạn gốc (chỉ trả lãi), lãi ưu đãi 6%/năm.
        let result = LoanCalculator.calculate(
            principalAmount: 100_000_000.0,
            termMonths: 12,
            baseInterestRate: 10.0,
            hasPreferential: true,
            preferentialMonths: 3,
            preferentialRate: 6.0,
            gracePeriodMonths: 3,
            method: .reducingBalance
        )

        // 3 tháng đầu gốc trả = 0
        XCTAssertEqual(result.schedule[0].principalPayment, 0.0, accuracy: 0.01)
        XCTAssertEqual(result.schedule[1].principalPayment, 0.0, accuracy: 0.01)
        XCTAssertEqual(result.schedule[2].principalPayment, 0.0, accuracy: 0.01)

        // Tháng 1: Lãi = 100tr * (6% / 12) = 500.000 ₫
        XCTAssertEqual(result.schedule[0].interestPayment, 500_000.0, accuracy: 1.0)

        // Cuối kỳ dư nợ = 0
        XCTAssertEqual(result.schedule.last?.remainingBalance ?? -1, 0.0, accuracy: 0.01)
    }

    func testLoanAnnuityFixedInstallment() {
        // Trả góp đều (Niên kim cố định)
        let result = LoanCalculator.calculate(
            principalAmount: 100_000_000.0,
            termMonths: 12,
            baseInterestRate: 12.0,
            method: .annuity
        )

        XCTAssertEqual(result.schedule.count, 12)
        let pmt = result.firstMonthPayment
        XCTAssertTrue(pmt > 8_800_000.0 && pmt < 9_000_000.0)
    }

    func testSavingsEndOfTerm() {
        let result = SavingsCalculator.calculate(
            depositAmount: 100_000_000.0,
            termMonths: 12,
            annualInterestRate: 6.0,
            paymentType: .endOfTerm
        )
        // 100tr * 6% = 6.000.000 ₫ lãi
        XCTAssertEqual(result.totalInterest, 6_000_000.0, accuracy: 1.0)
        XCTAssertEqual(result.totalMaturityAmount, 106_000_000.0, accuracy: 1.0)
    }

    func testCurrencyUtilsAmountToWords() {
        XCTAssertEqual(CurrencyUtils.amountToWords(0), "Không đồng")
        XCTAssertEqual(CurrencyUtils.amountToWords(1_000_000_000), "Một tỷ đồng")
        XCTAssertEqual(CurrencyUtils.amountToWords(500_000_000), "Năm trăm triệu đồng")
        XCTAssertEqual(CurrencyUtils.amountToWords(120_500_000), "Một trăm hai mươi triệu năm trăm nghìn đồng")
    }

    func testDepositCalculatorMultiSource() {
        let start = "15/01/2026"
        let items = [
            DepositSourceItem(
                name: "Sổ Quầy",
                sourceType: .counter,
                principalAmount: 200_000_000.0,
                maturityDate: "15/01/2027"
            ),
            DepositSourceItem(
                name: "Sổ Online",
                sourceType: .online,
                principalAmount: 100_000_000.0,
                maturityDate: "15/07/2026"
            )
        ]

        let result = DepositCalculator.calculate(
            items: items,
            startDateStr: start,
            counterRate: 6.0,
            onlineRate: 6.0,
            paymentType: .endOfTerm,
            isCompounding: false
        )

        // 200tr * 6% * 12/12 = 12tr; 100tr * 6% * 6/12 = 3tr -> tổng lãi = 15tr
        XCTAssertEqual(result.totalPrincipal, 300_000_000.0, accuracy: 1.0)
        XCTAssertEqual(result.totalInterest, 15_000_000.0, accuracy: 5.0)
        XCTAssertEqual(result.totalMaturityAmount, 315_000_000.0, accuracy: 5.0)
        XCTAssertEqual(result.schedule.count, 12)
    }

    func testDepositCalculatorMonthlyPayout() {
        let start = "15/01/2026"
        let items = [
            DepositSourceItem(
                name: "Sổ Hàng Tháng",
                sourceType: .online,
                principalAmount: 120_000_000.0,
                maturityDate: "15/01/2027"
            )
        ]

        let result = DepositCalculator.calculate(
            items: items,
            startDateStr: start,
            counterRate: 5.0,
            onlineRate: 6.0,
            paymentType: .monthly,
            isCompounding: false
        )

        // 120tr * 6% / 12 = 600.000 đ / tháng, 12 kỳ -> 7.200.000 đ
        XCTAssertEqual(result.firstMonthPayout, 600_000.0, accuracy: 5.0)
        XCTAssertEqual(result.totalInterest, 7_200_000.0, accuracy: 10.0)
        XCTAssertEqual(result.schedule.count, 12)
    }
}

