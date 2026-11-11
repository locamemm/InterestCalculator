//
//  TermInputField.swift
//  InterestCalculator
//

import SwiftUI

struct TermInputField: View {
    let label: String
    @Binding var termMonths: Int
    var minMonths: Int = 1
    var maxMonths: Int = 360
    var quickTerms: [(title: String, months: Int)] = []
    var accentColor: Color = AppTheme.primary
    
    @State private var textValue: String = ""
    @State private var selectedStepMonths: Int = 1
    @FocusState private var isFocused: Bool
    
    private let stepOptions: [(months: Int, label: String)] = [
        (1, "+1 th"),
        (3, "+3 th"),
        (6, "+6 th"),
        (12, "+12 th (1 năm)")
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
                    let next = max(minMonths, termMonths - selectedStepMonths)
                    termMonths = next
                    textValue = "\(next)"
                } label: {
                    Image(systemName: "minus.circle.fill")
                        .font(.title3)
                        .foregroundColor(.red)
                }
                .buttonStyle(.plain)
                
                TextField("Số tháng", text: $textValue)
                    .keyboardType(.numberPad)
                    .font(.system(size: 19, weight: .bold, design: .rounded))
                    .focused($isFocused)
                    .onChange(of: textValue) { newValue in
                        if let val = Int(newValue) {
                            termMonths = min(maxMonths, max(0, val))
                        } else if newValue.isEmpty {
                            termMonths = 0
                        }
                    }
                
                Text("tháng")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(accentColor)
                
                if termMonths > 0 {
                    Button {
                        termMonths = 0
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
            
            // Helper text (converted to years)
            if termMonths > 0 {
                let y = termMonths / 12
                let m = termMonths % 12
                let yearDesc: String = {
                    if y > 0 && m > 0 { return "≈ \(y) năm \(m) tháng" }
                    if y > 0 { return "≈ \(y) năm" }
                    return "Thời hạn: \(termMonths) tháng"
                }()
                Text(yearDesc)
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(accentColor)
            }
            
            // Step increment chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(stepOptions, id: \.months) { step in
                        let isSelected = selectedStepMonths == step.months
                        Button {
                            selectedStepMonths = step.months
                            let next = min(maxMonths, termMonths + step.months)
                            termMonths = next
                            textValue = "\(next)"
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
            if termMonths > 0 {
                textValue = "\(termMonths)"
            }
        }
        .onChange(of: termMonths) { newTerm in
            if Int(textValue) != newTerm {
                textValue = newTerm > 0 ? "\(newTerm)" : ""
            }
        }
    }
}
