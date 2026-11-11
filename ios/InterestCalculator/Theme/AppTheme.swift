//
//  AppTheme.swift
//  InterestCalculator
//

import SwiftUI

enum AppTheme {
    static let primary = Color(red: 0x0D/255.0, green: 0x47/255.0, blue: 0xA1/255.0)
    static let primaryLight = Color(red: 0x19/255.0, green: 0x76/255.0, blue: 0xD2/255.0)
    static let primaryContainer = Color(red: 0xE3/255.0, green: 0xF2/255.0, blue: 0xFD/255.0)
    
    static let green = Color(red: 0x2E/255.0, green: 0x7D/255.0, blue: 0x32/255.0)
    static let greenLight = Color(red: 0x38/255.0, green: 0x8E/255.0, blue: 0x3C/255.0)
    static let greenContainer = Color(red: 0xE8/255.0, green: 0xF5/255.0, blue: 0xE9/255.0)
    
    static let orange = Color(red: 0xE6/255.0, green: 0x51/255.0, blue: 0x00/255.0)
    static let orangeContainer = Color(red: 0xFF/255.0, green: 0xF3/255.0, blue: 0xE0/255.0)
    
    static let teal = Color(red: 0x00/255.0, green: 0x69/255.0, blue: 0x5C/255.0)
    static let tealLight = Color(red: 0x00/255.0, green: 0x89/255.0, blue: 0x7B/255.0)
    static let tealContainer = Color(red: 0xE0/255.0, green: 0xF2/255.0, blue: 0xF1/255.0)
    
    static let gold = Color(red: 0xFF/255.0, green: 0xA0/255.0, blue: 0x00/255.0)
    static let goldContainer = Color(red: 0xFF/255.0, green: 0xF8/255.0, blue: 0xE1/255.0)
    
    static let background = Color(UIColor.systemGroupedBackground)
    static let cardBackground = Color(UIColor.secondarySystemGroupedBackground)
    
    static let primaryGradient = LinearGradient(
        colors: [primary, primaryLight],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let greenGradient = LinearGradient(
        colors: [green, greenLight],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let tealGradient = LinearGradient(
        colors: [teal, tealLight],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let orangeGradient = LinearGradient(
        colors: [orange, Color(red: 0xF5/255.0, green: 0x7C/255.0, blue: 0x00/255.0)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

