//
//  SavedParamsDialog.swift
//  InterestCalculator
//

import SwiftUI

struct SavedLoanParamsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let currentParams: SavedLoanParams
    let presets: [SavedLoanParams]
    let onLoad: (SavedLoanParams) -> Void
    let onSaveNew: (SavedLoanParams) -> Void
    let onDelete: (String) -> Void
    
    @State private var presetName: String = ""
    @State private var showSaveSection: Bool = false
    
    var body: some View {
        NavigationStack {
            List {
                Section {
                    if showSaveSection {
                        VStack(alignment: .leading, spacing: 8) {
                            TextField("Tên mẫu (VD: Gói mua nhà VIP)", text: $presetName)
                                .textFieldStyle(.roundedBorder)
                            HStack {
                                Button("Hủy") {
                                    showSaveSection = false
                                }
                                .foregroundColor(.secondary)
                                
                                Spacer()
                                
                                Button("Lưu mẫu hiện tại") {
                                    var toSave = currentParams
                                    toSave.id = UUID().uuidString
                                    toSave.name = presetName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Mẫu vay \(DateTimeUtils.todayString())" : presetName
                                    toSave.savedAt = Int64(Date().timeIntervalSince1970 * 1000)
                                    onSaveNew(toSave)
                                    showSaveSection = false
                                    presetName = ""
                                }
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.primary)
                            }
                        }
                        .padding(.vertical, 4)
                    } else {
                        Button {
                            showSaveSection = true
                        } label: {
                            Label("Lưu thông số hiện tại thành mẫu mới", systemImage: "plus.circle.fill")
                                .foregroundColor(AppTheme.primary)
                                .fontWeight(.semibold)
                        }
                    }
                } header: {
                    Text("Lưu mẫu mới")
                }
                
                Section {
                    if presets.isEmpty {
                        Text("Chưa có mẫu nào được lưu")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(presets) { p in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(p.name.isEmpty ? "Mẫu không tên" : p.name)
                                        .font(.headline)
                                    Spacer()
                                    Text(DateTimeUtils.formatMillis(p.savedAt))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                Text("\(CurrencyUtils.formatCompactVnd(p.principalAmount)) • \(p.termMonths) tháng • \(p.baseRate)%/năm")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text(p.selectedMethod.title)
                                    .font(.caption2)
                                    .foregroundColor(AppTheme.primary)
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onLoad(p)
                                dismiss()
                            }
                        }
                        .onDelete { indexSet in
                            for idx in indexSet {
                                onDelete(presets[idx].id)
                            }
                        }
                    }
                } header: {
                    Text("Danh sách mẫu đã lưu (\(presets.count))")
                }
            }
            .navigationTitle("Mẫu thông số vay")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
            .keyboardDoneToolbar()
        }
    }
}

struct SavedSavingsParamsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let currentParams: SavedSavingsParams
    let presets: [SavedSavingsParams]
    let onLoad: (SavedSavingsParams) -> Void
    let onSaveNew: (SavedSavingsParams) -> Void
    let onDelete: (String) -> Void
    
    @State private var presetName: String = ""
    @State private var showSaveSection: Bool = false
    
    var body: some View {
        NavigationStack {
            List {
                Section {
                    if showSaveSection {
                        VStack(alignment: .leading, spacing: 8) {
                            TextField("Tên mẫu (VD: Tiết kiệm VIP)", text: $presetName)
                                .textFieldStyle(.roundedBorder)
                            HStack {
                                Button("Hủy") {
                                    showSaveSection = false
                                }
                                .foregroundColor(.secondary)
                                
                                Spacer()
                                
                                Button("Lưu mẫu hiện tại") {
                                    var toSave = currentParams
                                    toSave.id = UUID().uuidString
                                    toSave.name = presetName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Mẫu gửi \(DateTimeUtils.todayString())" : presetName
                                    toSave.savedAt = Int64(Date().timeIntervalSince1970 * 1000)
                                    onSaveNew(toSave)
                                    showSaveSection = false
                                    presetName = ""
                                }
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.green)
                            }
                        }
                        .padding(.vertical, 4)
                    } else {
                        Button {
                            showSaveSection = true
                        } label: {
                            Label("Lưu thông số hiện tại thành mẫu mới", systemImage: "plus.circle.fill")
                                .foregroundColor(AppTheme.green)
                                .fontWeight(.semibold)
                        }
                    }
                } header: {
                    Text("Lưu mẫu mới")
                }
                
                Section {
                    if presets.isEmpty {
                        Text("Chưa có mẫu nào được lưu")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(presets) { p in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(p.name.isEmpty ? "Mẫu không tên" : p.name)
                                        .font(.headline)
                                    Spacer()
                                    Text(DateTimeUtils.formatMillis(p.savedAt))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                Text("\(CurrencyUtils.formatCompactVnd(p.depositAmount)) • \(p.termMonths) tháng • \(p.interestRate)%/năm")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text(p.paymentType.title)
                                    .font(.caption2)
                                    .foregroundColor(AppTheme.green)
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onLoad(p)
                                dismiss()
                            }
                        }
                        .onDelete { indexSet in
                            for idx in indexSet {
                                onDelete(presets[idx].id)
                            }
                        }
                    }
                } header: {
                    Text("Danh sách mẫu đã lưu (\(presets.count))")
                }
            }
            .navigationTitle("Mẫu gửi tiết kiệm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
            .keyboardDoneToolbar()
        }
    }
}

struct SavedDebtParamsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let currentParams: SavedDebtParams
    let presets: [SavedDebtParams]
    let onLoad: (SavedDebtParams) -> Void
    let onSaveNew: (SavedDebtParams) -> Void
    let onDelete: (String) -> Void
    
    @State private var presetName: String = ""
    @State private var showSaveSection: Bool = false
    
    var body: some View {
        NavigationStack {
            List {
                Section {
                    if showSaveSection {
                        VStack(alignment: .leading, spacing: 8) {
                            TextField("Tên mẫu danh mục nợ", text: $presetName)
                                .textFieldStyle(.roundedBorder)
                            HStack {
                                Button("Hủy") {
                                    showSaveSection = false
                                }
                                .foregroundColor(.secondary)
                                
                                Spacer()
                                
                                Button("Lưu mẫu hiện tại") {
                                    var toSave = currentParams
                                    toSave.id = UUID().uuidString
                                    toSave.name = presetName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Mẫu danh mục \(DateTimeUtils.todayString())" : presetName
                                    toSave.savedAt = Int64(Date().timeIntervalSince1970 * 1000)
                                    onSaveNew(toSave)
                                    showSaveSection = false
                                    presetName = ""
                                }
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.orange)
                            }
                        }
                        .padding(.vertical, 4)
                    } else {
                        Button {
                            showSaveSection = true
                        } label: {
                            Label("Lưu danh mục hiện tại thành mẫu mới", systemImage: "plus.circle.fill")
                                .foregroundColor(AppTheme.orange)
                                .fontWeight(.semibold)
                        }
                    }
                } header: {
                    Text("Lưu mẫu mới")
                }
                
                Section {
                    if presets.isEmpty {
                        Text("Chưa có mẫu nào được lưu")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(presets) { p in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(p.name.isEmpty ? "Mẫu không tên" : p.name)
                                        .font(.headline)
                                    Spacer()
                                    Text(DateTimeUtils.formatMillis(p.savedAt))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                let totalDebt = p.items.reduce(0.0) { $0 + $1.currentDebt }
                                Text("\(CurrencyUtils.formatCompactVnd(totalDebt)) • \(p.items.count) khoản nợ")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onLoad(p)
                                dismiss()
                            }
                        }
                        .onDelete { indexSet in
                            for idx in indexSet {
                                onDelete(presets[idx].id)
                            }
                        }
                    }
                } header: {
                    Text("Danh sách mẫu đã lưu (\(presets.count))")
                }
            }
            .navigationTitle("Mẫu danh mục nợ")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
            .keyboardDoneToolbar()
        }
    }
}

struct SavedDepositParamsSheet: View {
    @Environment(\.dismiss) private var dismiss
    let currentParams: SavedDepositParams
    let presets: [SavedDepositParams]
    let onLoad: (SavedDepositParams) -> Void
    let onSaveNew: (SavedDepositParams) -> Void
    let onDelete: (String) -> Void
    
    @State private var presetName: String = ""
    @State private var showSaveSection: Bool = false
    
    var body: some View {
        NavigationStack {
            List {
                Section {
                    if showSaveSection {
                        VStack(alignment: .leading, spacing: 8) {
                            TextField("Tên mẫu danh mục tiền gửi", text: $presetName)
                                .textFieldStyle(.roundedBorder)
                            HStack {
                                Button("Hủy") {
                                    showSaveSection = false
                                }
                                .foregroundColor(.secondary)
                                
                                Spacer()
                                
                                Button("Lưu mẫu hiện tại") {
                                    var toSave = currentParams
                                    toSave.id = UUID().uuidString
                                    toSave.name = presetName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Mẫu tiền gửi \(DateTimeUtils.todayString())" : presetName
                                    toSave.savedAt = Int64(Date().timeIntervalSince1970 * 1000)
                                    onSaveNew(toSave)
                                    showSaveSection = false
                                    presetName = ""
                                }
                                .fontWeight(.bold)
                                .foregroundColor(AppTheme.teal)
                            }
                        }
                        .padding(.vertical, 4)
                    } else {
                        Button {
                            showSaveSection = true
                        } label: {
                            Label("Lưu danh mục hiện tại thành mẫu mới", systemImage: "plus.circle.fill")
                                .foregroundColor(AppTheme.teal)
                                .fontWeight(.semibold)
                        }
                    }
                } header: {
                    Text("Lưu mẫu mới")
                }
                
                Section {
                    if presets.isEmpty {
                        Text("Chưa có mẫu nào được lưu")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(presets) { p in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Text(p.name.isEmpty ? "Mẫu không tên" : p.name)
                                        .font(.headline)
                                    Spacer()
                                    Text(DateTimeUtils.formatMillis(p.savedAt))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                let totalPrincipal = p.items.reduce(0.0) { $0 + $1.principalAmount }
                                Text("\(CurrencyUtils.formatCompactVnd(totalPrincipal)) • \(p.items.count) sổ gửi")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onLoad(p)
                                dismiss()
                            }
                        }
                        .onDelete { indexSet in
                            for idx in indexSet {
                                onDelete(presets[idx].id)
                            }
                        }
                    }
                } header: {
                    Text("Danh sách mẫu đã lưu (\(presets.count))")
                }
            }
            .navigationTitle("Mẫu danh mục tiền gửi")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Đóng") { dismiss() }
                }
            }
            .keyboardDoneToolbar()
        }
    }
}

