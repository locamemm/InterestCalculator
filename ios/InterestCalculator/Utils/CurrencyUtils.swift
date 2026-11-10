//
//  CurrencyUtils.swift
//  InterestCalculator
//

import Foundation

enum CurrencyUtils {
    private static let formatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        formatter.decimalSeparator = ","
        formatter.maximumFractionDigits = 0
        return formatter
    }()
    
    private static let decimalFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        formatter.decimalSeparator = ","
        formatter.minimumFractionDigits = 1
        formatter.maximumFractionDigits = 1
        return formatter
    }()
    
    private static let scheduleDecimalFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        formatter.decimalSeparator = ","
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()
    
    static func formatVnd(_ amount: Double) -> String {
        let numStr = formatter.string(from: NSNumber(value: Int64(amount))) ?? "0"
        return "\(numStr) ₫"
    }
    
    static func formatVnd(_ amount: Int64) -> String {
        let numStr = formatter.string(from: NSNumber(value: amount)) ?? "0"
        return "\(numStr) ₫"
    }
    
    static func formatNumber(_ amount: Double) -> String {
        return formatter.string(from: NSNumber(value: Int64(amount))) ?? "0"
    }
    
    static func formatNumber(_ amount: Int64) -> String {
        return formatter.string(from: NSNumber(value: amount)) ?? "0"
    }
    
    static func formatCompactVnd(_ amount: Double) -> String {
        let absVal = abs(amount)
        if absVal >= 1_000_000 {
            let tr = amount / 1_000_000.0
            if tr.truncatingRemainder(dividingBy: 1.0) == 0 {
                return "\(formatter.string(from: NSNumber(value: Int64(tr))) ?? "0") triệu"
            } else {
                return "\(decimalFormatter.string(from: NSNumber(value: tr)) ?? "0") triệu"
            }
        } else if absVal >= 1_000 {
            let k = amount / 1_000.0
            if k.truncatingRemainder(dividingBy: 1.0) == 0 {
                return "\(formatter.string(from: NSNumber(value: Int64(k))) ?? "0")k"
            } else {
                return "\(decimalFormatter.string(from: NSNumber(value: k)) ?? "0")k"
            }
        } else {
            return formatVnd(amount)
        }
    }
    
    static func formatScheduleVnd(_ amount: Double) -> String {
        let absVal = abs(amount)
        if absVal >= 1_000_000 {
            let tr = amount / 1_000_000.0
            let roundedTr = round(tr)
            if abs(tr - roundedTr) < 1e-6 {
                return "\(formatter.string(from: NSNumber(value: Int64(roundedTr))) ?? "0") tr"
            } else {
                return "\(scheduleDecimalFormatter.string(from: NSNumber(value: tr)) ?? "0") tr"
            }
        } else if absVal >= 1_000 {
            let k = amount / 1_000.0
            let roundedK = round(k)
            if abs(k - roundedK) < 1e-6 {
                return "\(formatter.string(from: NSNumber(value: Int64(roundedK))) ?? "0")k"
            } else {
                return "\(scheduleDecimalFormatter.string(from: NSNumber(value: k)) ?? "0")k"
            }
        } else {
            return formatVnd(amount)
        }
    }
    
    static func formatMillions(_ amount: Double) -> String {
        let tr = amount / 1_000_000.0
        if tr.truncatingRemainder(dividingBy: 1.0) == 0 {
            return "\(formatter.string(from: NSNumber(value: Int64(tr))) ?? "0") triệu"
        } else {
            return "\(decimalFormatter.string(from: NSNumber(value: tr)) ?? "0") triệu"
        }
    }
    
    static func parseAmount(_ text: String) -> Double {
        let clean = text
            .replacingOccurrences(of: ".", with: "")
            .replacingOccurrences(of: ",", with: "")
            .replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: "₫", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        return Double(clean) ?? 0.0
    }
    
    /// Đọc số tiền thành chữ tiếng Việt chuẩn ngân hàng
    static func amountToWords(_ amount: Int64) -> String {
        if amount == 0 { return "Không đồng" }
        var n = amount
        let units = ["", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"]
        let digits = ["không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"]
        
        func readThreeDigits(_ number: Int, isFirstGroup: Bool) -> String {
            let h = number / 100
            let t = (number % 100) / 10
            let u = number % 10
            var parts: [String] = []
            
            if h > 0 || !isFirstGroup {
                parts.append("\(digits[h]) trăm")
            }
            
            if t > 1 {
                parts.append("\(digits[t]) mươi")
                if u == 1 {
                    parts.append("mốt")
                } else if u == 5 {
                    parts.append("lăm")
                } else if u > 0 {
                    parts.append(digits[u])
                }
            } else if t == 1 {
                parts.append("mười")
                if u == 5 {
                    parts.append("lăm")
                } else if u > 0 {
                    parts.append(digits[u])
                }
            } else {
                if u > 0 {
                    if h > 0 || !isFirstGroup {
                        parts.append("lẻ")
                    }
                    parts.append(digits[u])
                }
            }
            return parts.joined(separator: " ")
        }
        
        var groups: [Int] = []
        while n > 0 {
            groups.append(Int(n % 1000))
            n /= 1000
        }
        
        var resultWords: [String] = []
        for i in stride(from: groups.count - 1, through: 0, by: -1) {
            let groupVal = groups[i]
            if groupVal > 0 {
                let groupStr = readThreeDigits(groupVal, isFirstGroup: i == groups.count - 1)
                resultWords.append(groupStr)
                if !units[i].isEmpty {
                    resultWords.append(units[i])
                }
            }
        }
        
        let rawText = resultWords.joined(separator: " ").trimmingCharacters(in: .whitespacesAndNewlines)
        if rawText.isEmpty { return "Không đồng" }
        
        let firstLetter = rawText.prefix(1).uppercased()
        let remainder = rawText.dropFirst()
        return "\(firstLetter)\(remainder) đồng"
    }
}
