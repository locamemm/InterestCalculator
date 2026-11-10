//
//  DateTimeUtils.swift
//  InterestCalculator
//

import Foundation

enum DateTimeUtils {
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        formatter.locale = Locale(identifier: "vi_VN")
        formatter.timeZone = TimeZone.current
        return formatter
    }()
    
    static func todayString() -> String {
        return dateFormatter.string(from: Date())
    }
    
    static func formatDate(_ date: Date) -> String {
        return dateFormatter.string(from: date)
    }
    
    static func parseDate(_ dateStr: String) -> Date {
        return dateFormatter.date(from: dateStr.trimmingCharacters(in: .whitespacesAndNewlines)) ?? Date()
    }
    
    static func addMonths(_ startDateStr: String, _ months: Int) -> String {
        let date = parseDate(startDateStr)
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone.current
        if let newDate = cal.date(byAdding: .month, value: months, to: date) {
            return dateFormatter.string(from: newDate)
        }
        return startDateStr
    }
    
    static func addDays(_ startDateStr: String, _ days: Int) -> String {
        let date = parseDate(startDateStr)
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone.current
        if let newDate = cal.date(byAdding: .day, value: days, to: date) {
            return dateFormatter.string(from: newDate)
        }
        return startDateStr
    }
    
    static func tomorrowString() -> String {
        return addDays(todayString(), 1)
    }
    
    static func firstOfNextMonthString() -> String {
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone.current
        var comps = cal.dateComponents([.year, .month], from: Date())
        comps.month = (comps.month ?? 1) + 1
        comps.day = 1
        if let date = cal.date(from: comps) {
            return dateFormatter.string(from: date)
        }
        return todayString()
    }
    
    static func formatMillis(_ millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(millis) / 1000.0)
        return dateFormatter.string(from: date)
    }
    
    static func toMillis(_ dateStr: String) -> Int64 {
        let date = parseDate(dateStr)
        return Int64(date.timeIntervalSince1970 * 1000)
    }
    
    static func getPeriodDate(_ startDateStr: String, _ periodMonth: Int) -> String {
        return addMonths(startDateStr, periodMonth)
    }
    
    static func daysUntil(_ targetDateStr: String) -> Int {
        let target = parseDate(targetDateStr)
        let today = Date()
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone.current
        
        let startOfToday = cal.startOfDay(for: today)
        let startOfTarget = cal.startOfDay(for: target)
        
        let components = cal.dateComponents([.day], from: startOfToday, to: startOfTarget)
        return components.day ?? 0
    }
    
    static func isExpiringSoon(_ maturityDateStr: String, thresholdDays: Int = 15) -> Bool {
        let days = daysUntil(maturityDateStr)
        return days >= 0 && days <= thresholdDays
    }
    
    static func formatShortYearDate(_ dateStr: String) -> String {
        let parts = dateStr.split(separator: "/")
        if parts.count == 3 {
            let day = String(parts[0]).paddingLeft(toLength: 2, withPad: "0")
            let month = String(parts[1]).paddingLeft(toLength: 2, withPad: "0")
            let year = String(parts[2])
            let shortYear = year.count >= 2 ? String(year.suffix(2)) : year
            return "\(day)/\(month)/\(shortYear)"
        }
        return dateStr
    }
    
    static func monthsBetween(_ startDateStr: String, _ endDateStr: String) -> Int {
        let startDate = parseDate(startDateStr)
        let endDate = parseDate(endDateStr)
        if endDate <= startDate { return 1 }
        
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone.current
        
        let startComps = cal.dateComponents([.year, .month, .day], from: startDate)
        let endComps = cal.dateComponents([.year, .month, .day], from: endDate)
        
        let yearDiff = (endComps.year ?? 0) - (startComps.year ?? 0)
        let monthDiff = (endComps.month ?? 0) - (startComps.month ?? 0)
        var totalMonths = yearDiff * 12 + monthDiff
        
        let dayDiff = (endComps.day ?? 0) - (startComps.day ?? 0)
        if dayDiff > 14 {
            totalMonths += 1
        }
        return max(1, totalMonths)
    }
}

private extension String {
    func paddingLeft(toLength: Int, withPad pad: String) -> String {
        if self.count >= toLength { return self }
        let needed = toLength - self.count
        return String(repeating: pad, count: needed) + self
    }
}
