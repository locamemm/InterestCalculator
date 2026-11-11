//
//  KeyboardHelper.swift
//  InterestCalculator
//

import SwiftUI
import UIKit

enum KeyboardHelper {
    static func hide() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

struct KeyboardDoneToolbarModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .toolbar {
                ToolbarItemGroup(placement: .keyboard) {
                    Spacer()
                    Button {
                        KeyboardHelper.hide()
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "keyboard.chevron.compact.down")
                            Text("Ẩn bàn phím")
                        }
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(AppTheme.primary)
                    }
                }
            }
    }
}

extension View {
    func keyboardDoneToolbar() -> some View {
        self.modifier(KeyboardDoneToolbarModifier())
    }
    
    func hideKeyboard() {
        KeyboardHelper.hide()
    }
}

