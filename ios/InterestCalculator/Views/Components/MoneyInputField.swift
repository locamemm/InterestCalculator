//
//  MoneyInputField.swift
//  InterestCalculator
//

import SwiftUI

struct MoneyInputField: View {
    let label: String
    @Binding var amount: Double
    var quickAmounts: [Double] = [100_000_000, 500_000_000, 1_000_000_000, 2_000_000_000, 5_000_000_000]
    var accentColor: Color = AppTheme.primary
    
    @State private var textValue: String = ""
    @FocusState private var isFocused: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.secondary)
            
            HStack {
                TextField("Nhập số tiền", text: $textValue)
                    .keyboardType(.numberPad)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
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
                }
            }
            .padding(12)
            .background(Color(UIColor.secondarySystemGroupedBackground))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isFocused ? accentColor : Color.gray.opacity(0.2), lineWidth: 1.5)
            )
            
            // Read in Vietnamese Words
            if amount > 0 {
                Text("Bằng chữ: \(CurrencyUtils.amountToWords(Int64(amount)))")
                    .font(.caption)
                    .foregroundColor(accentColor)
                    .lineLimit(2)
            }
            
            // Quick preset chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(quickAmounts, id: \.self) { qVal in
                        Button {
                            amount = qVal
                            textValue = CurrencyUtils.formatNumber(qVal)
                        } label: {
                            Text(CurrencyUtils.formatCompactVnd(qVal))
                                .font(.caption)
                                .fontWeight(amount == qVal ? .bold : .medium)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(amount == qVal ? accentColor.opacity(0.15) : Color.gray.opacity(0.1))
                                .foregroundColor(amount == qVal ? accentColor : .primary)
                                .cornerRadius(8)
                        }
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
            if parsed != newAmount {
                textValue = newAmount > 0 ? CurrencyUtils.formatNumber(newAmount) : ""
            }
        }
    }
}
