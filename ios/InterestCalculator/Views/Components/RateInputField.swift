//
//  RateInputField.swift
//  InterestCalculator
//

import SwiftUI

struct RateInputField: View {
    let label: String
    @Binding var rate: Double
    @Binding var isMonthly: Bool
    var quickRates: [Double] = [5.5, 6.8, 8.5, 9.5, 10.5, 12.0]
    var accentColor: Color = AppTheme.primary
    
    @State private var textValue: String = ""
    @FocusState private var isFocused: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(label)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                // Toggle %/năm <-> %/tháng
                Picker("", selection: $isMonthly) {
                    Text("%/năm").tag(false)
                    Text("%/tháng").tag(true)
                }
                .pickerStyle(.segmented)
                .frame(width: 150)
            }
            
            HStack {
                TextField("0.0", text: $textValue)
                    .keyboardType(.decimalPad)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                    .focused($isFocused)
                    .onChange(of: textValue) { newValue in
                        let cleaned = newValue.replacingOccurrences(of: ",", with: ".")
                        if let d = Double(cleaned) {
                            rate = d
                        }
                    }
                
                Text(isMonthly ? "%/tháng" : "%/năm")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(accentColor)
            }
            .padding(12)
            .background(Color(UIColor.secondarySystemGroupedBackground))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isFocused ? accentColor : Color.gray.opacity(0.2), lineWidth: 1.5)
            )
            
            // Equivalent rate helper
            if rate > 0 {
                let eqText: String = isMonthly
                    ? "Tương đương ~ \(String(format: "%.2f", rate * 12.0))%/năm"
                    : "Tương đương ~ \(String(format: "%.2f", rate / 12.0))%/tháng"
                Text(eqText)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            // Quick chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(quickRates, id: \.self) { qRate in
                        Button {
                            rate = qRate
                            textValue = String(format: "%.1f", qRate)
                        } label: {
                            Text("\(String(format: "%.1f", qRate))%")
                                .font(.caption)
                                .fontWeight(rate == qRate ? .bold : .medium)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(rate == qRate ? accentColor.opacity(0.15) : Color.gray.opacity(0.1))
                                .foregroundColor(rate == qRate ? accentColor : .primary)
                                .cornerRadius(8)
                        }
                    }
                }
            }
        }
        .onAppear {
            if rate > 0 {
                textValue = String(format: "%.1f", rate)
            }
        }
        .onChange(of: rate) { newRate in
            let cleaned = textValue.replacingOccurrences(of: ",", with: ".")
            if Double(cleaned) != newRate {
                textValue = String(format: "%.1f", newRate)
            }
        }
    }
}

