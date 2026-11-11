//
//  RateInputField.swift
//  InterestCalculator
//

import SwiftUI

struct RateInputField: View {
    let label: String
    @Binding var rate: Double
    @Binding var isMonthly: Bool
    var minRate: Double = 0.0
    var maxRate: Double = 50.0
    var allowPeriodToggle: Bool = true
    var quickRates: [Double] = []
    var accentColor: Color = AppTheme.primary
    
    @State private var textValue: String = ""
    @State private var selectedStepRate: Double = 0.1
    @FocusState private var isFocused: Bool
    
    private var stepOptions: [(value: Double, label: String)] {
        if isMonthly {
            return [
                (0.05, "+0.05%"),
                (0.1, "+0.1%"),
                (0.2, "+0.2%"),
                (0.5, "+0.5%")
            ]
        } else {
            return [
                (0.1, "+0.1%"),
                (0.2, "+0.2%"),
                (0.5, "+0.5%"),
                (1.0, "+1.0%")
            ]
        }
    }
    
    private func formatRate(_ val: Double) -> String {
        if val <= 0 { return "" }
        let rounded = round(val * 100.0) / 100.0
        if rounded.truncatingRemainder(dividingBy: 1.0) == 0 {
            return "\(Int(rounded))"
        }
        var str = String(format: "%.2f", rounded)
        while str.hasSuffix("0") { str.removeLast() }
        if str.hasSuffix(".") { str.removeLast() }
        return str
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(label)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                if allowPeriodToggle {
                    Picker("", selection: Binding(
                        get: { isMonthly },
                        set: { newMonthly in
                            guard newMonthly != isMonthly else { return }
                            isMonthly = newMonthly
                            selectedStepRate = 0.1
                            if rate > 0 {
                                let converted = newMonthly ? (rate / 12.0) : (rate * 12.0)
                                let rounded = round(converted * 100.0) / 100.0
                                rate = rounded
                                textValue = formatRate(rounded)
                            }
                        }
                    )) {
                        Text("%/năm").tag(false)
                        Text("%/tháng").tag(true)
                    }
                    .pickerStyle(.segmented)
                    .frame(width: 150)
                }
            }
            
            HStack(spacing: 8) {
                // Subtract step button
                Button {
                    let next = max(minRate, round((rate - selectedStepRate) * 100.0) / 100.0)
                    rate = next
                    textValue = formatRate(next)
                } label: {
                    Image(systemName: "minus.circle.fill")
                        .font(.title3)
                        .foregroundColor(.red)
                }
                .buttonStyle(.plain)
                
                TextField("0.0", text: $textValue)
                    .keyboardType(.decimalPad)
                    .font(.system(size: 19, weight: .bold, design: .rounded))
                    .focused($isFocused)
                    .onChange(of: textValue) { newValue in
                        let cleaned = newValue.replacingOccurrences(of: ",", with: ".")
                        if let d = Double(cleaned) {
                            rate = d
                        } else if cleaned.isEmpty {
                            rate = 0.0
                        }
                    }
                
                Text(isMonthly ? "%/tháng" : "%/năm")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(accentColor)
                
                if rate > 0 {
                    Button {
                        rate = 0.0
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
            
            // Equivalent rate helper
            if rate > 0 {
                let eqText: String = isMonthly
                    ? "≈ \(formatRate(rate * 12.0))%/năm (Quy đổi 1 tháng × 12)"
                    : "≈ \(formatRate(rate / 12.0))%/tháng (Quy đổi 1 năm = 12 tháng)"
                Text(eqText)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            // Step increment chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(stepOptions, id: \.value) { step in
                        let isSelected = abs(selectedStepRate - step.value) < 0.001
                        Button {
                            selectedStepRate = step.value
                            let maxCap = isMonthly ? 10.0 : maxRate
                            let next = min(maxCap, round((rate + step.value) * 100.0) / 100.0)
                            rate = next
                            textValue = formatRate(next)
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
            if rate > 0 {
                textValue = formatRate(rate)
            }
        }
        .onChange(of: rate) { newRate in
            let cleaned = textValue.replacingOccurrences(of: ",", with: ".")
            let currentParsed = Double(cleaned) ?? 0.0
            if abs(currentParsed - newRate) > 0.001 {
                textValue = formatRate(newRate)
            }
        }
    }
}
