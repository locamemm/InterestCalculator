//
//  TermInputField.swift
//  InterestCalculator
//

import SwiftUI

struct TermInputField: View {
    let label: String
    @Binding var termMonths: Int
    var quickTerms: [(title: String, months: Int)] = [
        ("6 tháng", 6),
        ("12 tháng", 12),
        ("2 năm", 24),
        ("3 năm", 36),
        ("5 năm", 60),
        ("10 năm", 120),
        ("15 năm", 180),
        ("20 năm", 240),
        ("25 năm", 300)
    ]
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
                TextField("Số tháng", text: $textValue)
                    .keyboardType(.numberPad)
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                    .focused($isFocused)
                    .onChange(of: textValue) { newValue in
                        if let val = Int(newValue) {
                            termMonths = val
                        }
                    }
                
                Text("tháng")
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
            
            // Helper text (converted to years)
            if termMonths > 0 {
                let y = termMonths / 12
                let m = termMonths % 12
                let yStr = y > 0 ? "\(y) năm " : ""
                let mStr = m > 0 ? "\(m) tháng" : ""
                Text("Thời hạn: \(termMonths) tháng (~ \(yStr)\(mStr))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            // Quick chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(quickTerms, id: \.months) { item in
                        Button {
                            termMonths = item.months
                            textValue = "\(item.months)"
                        } label: {
                            Text(item.title)
                                .font(.caption)
                                .fontWeight(termMonths == item.months ? .bold : .medium)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(termMonths == item.months ? accentColor.opacity(0.15) : Color.gray.opacity(0.1))
                                .foregroundColor(termMonths == item.months ? accentColor : .primary)
                                .cornerRadius(8)
                        }
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
                textValue = "\(newTerm)"
            }
        }
    }
}
