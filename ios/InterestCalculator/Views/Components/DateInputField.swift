//
//  DateInputField.swift
//  InterestCalculator
//

import SwiftUI

struct DateInputField: View {
    let label: String
    @Binding var dateStr: String
    var accentColor: Color = AppTheme.primary
    
    @State private var showPicker: Bool = false
    @State private var selectedDate: Date = Date()
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundColor(.secondary)
            
            Button {
                selectedDate = DateTimeUtils.parseDate(dateStr)
                showPicker = true
            } label: {
                HStack {
                    Image(systemName: "calendar")
                        .foregroundColor(accentColor)
                    
                    Text(dateStr.isEmpty ? DateTimeUtils.todayString() : dateStr)
                        .font(.system(size: 16, weight: .semibold, design: .rounded))
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }
                .padding(12)
                .background(Color(UIColor.secondarySystemGroupedBackground))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                )
            }
            
            // Quick date buttons
            HStack(spacing: 8) {
                Button {
                    dateStr = DateTimeUtils.todayString()
                } label: {
                    Text("Hôm nay")
                        .font(.caption)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.gray.opacity(0.1))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
                
                Button {
                    dateStr = DateTimeUtils.tomorrowString()
                } label: {
                    Text("Ngày mai")
                        .font(.caption)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.gray.opacity(0.1))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
                
                Button {
                    dateStr = DateTimeUtils.firstOfNextMonthString()
                } label: {
                    Text("Đầu tháng tới")
                        .font(.caption)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.gray.opacity(0.1))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                }
            }
        }
        .sheet(isPresented: $showPicker) {
            NavigationStack {
                VStack {
                    DatePicker(
                        "Chọn ngày",
                        selection: $selectedDate,
                        displayedComponents: [.date]
                    )
                    .datePickerStyle(.graphical)
                    .padding()
                    
                    Spacer()
                }
                .navigationTitle(label)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Đóng") { showPicker = false }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Chọn") {
                            dateStr = DateTimeUtils.formatDate(selectedDate)
                            showPicker = false
                        }
                        .fontWeight(.bold)
                    }
                }
            }
            .presentationDetents([.medium])
        }
    }
}

