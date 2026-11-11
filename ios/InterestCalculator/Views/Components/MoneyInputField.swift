//
//  MoneyInputField.swift
//  InterestCalculator
//

import SwiftUI

struct MoneyInputField: View {
    let label: String
    @Binding var amount: Double
    var accentColor: Color = AppTheme.primary
    
    @State private var textValue: String = ""
    @State private var selectedStepAmount: Double = 100_000_000
    @FocusState private var isFocused: Bool
    
    private let stepOptions: [(value: Double, label: String)] = [
        (10_000_000, "+10 triệu"),
        (50_000_000, "+50 triệu"),
        (100_000_000, "+100 triệu"),
        (500_000_000, "+500 triệu"),
        (1_000_000_000, "+1.000 triệu")
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.secondary)
            
            HStack(spacing: 8) {
                // Subtract step button
                Button {
                    let next = max(0, amount - selectedStepAmount)
                    amount = next
                    textValue = next > 0 ? CurrencyUtils.formatNumber(next) : ""
                } label: {
                    Image(systemName: "minus.circle.fill")
                        .font(.title3)
                        .foregroundColor(.red)
                }
                .buttonStyle(.plain)
                
                TextField("Nhập số tiền", text: $textValue)
                    .keyboardType(.numberPad)
                    .font(.system(size: 19, weight: .bold, design: .rounded))
                    .focused($isFocused)
                    .onChange(of: textValue) { newValue in
                        let parsed = CurrencyUtils.parseAmount(newValue)
                        if parsed != amount {
                            amount = parsed
                        }
                    }
                
                Text("₫")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(accentColor)
                
                if amount > 0 {
                    Button {
                        amount = 0
                        textValue = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(12)
            .background(Color(UIColor.secondarySystemGroupedBackground))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isFocused ? accentColor : Color.gray.opacity(0.2), lineWidth: 1.5)
            )
            
            // Millions & Vietnamese words helper
            if amount >= 1_000_000 {
                HStack(spacing: 4) {
                    Text("≈ \(CurrencyUtils.formatMillions(amount))")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(accentColor)
                    Text("(\(CurrencyUtils.amountToWords(Int64(amount))))")
                        .font(.caption2)
                        .italic()
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            } else if amount > 0 {
                Text("Bằng chữ: \(CurrencyUtils.amountToWords(Int64(amount)))")
                    .font(.caption)
                    .foregroundColor(accentColor)
                    .lineLimit(2)
            }
            
            // Step increment chips (tapping selects step & adds to current amount)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(stepOptions, id: \.value) { step in
                        let isSelected = abs(selectedStepAmount - step.value) < 1.0
                        Button {
                            selectedStepAmount = step.value
                            let next = amount + step.value
                            amount = next
                            textValue = CurrencyUtils.formatNumber(next)
                        } label: {
                            Text(step.label)
                                .font(.caption)
                                .fontWeight(isSelected ? .bold : .medium)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(isSelected ? accentColor.opacity(0.18) : Color.gray.opacity(0.1))
                                .foregroundColor(isSelected ? accentColor : .primary)
                                .cornerRadius(10)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(isSelected ? accentColor.opacity(0.5) : Color.clear, lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .onAppear {
            if amount > 0 {
                textValue = CurrencyUtils.formatNumber(amount)
            }
        }
        .onChange(of: amount) { newAmount in
            let parsed = CurrencyUtils.parseAmount(textValue)
            if abs(parsed - newAmount) > 1.0 {
                textValue = newAmount > 0 ? CurrencyUtils.formatNumber(newAmount) : ""
            }
        }
    }
}
