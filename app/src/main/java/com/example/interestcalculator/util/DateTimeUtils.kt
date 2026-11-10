package com.example.interestcalculator.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun todayString(): String {
        return dateFormat.format(Date())
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun parseDate(dateStr: String): Date {
        return try {
            dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun addMonths(startDateStr: String, months: Int): String {
        val date = parseDate(startDateStr)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, months)
        return dateFormat.format(cal.time)
    }

    fun addDays(startDateStr: String, days: Int): String {
        val date = parseDate(startDateStr)
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        return dateFormat.format(cal.time)
    }

    fun tomorrowString(): String {
        return addDays(todayString(), 1)
    }

    fun firstOfNextMonthString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return dateFormat.format(cal.time)
    }

    fun formatMillis(millis: Long): String {
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = millis
        }
        return dateFormat.format(cal.time)
    }

    fun toMillis(dateStr: String): Long {
        val date = parseDate(dateStr)
        return date.time
    }

    fun getPeriodDate(startDateStr: String, periodMonth: Int): String {
        return addMonths(startDateStr, periodMonth)
    }

    /**
     * Số ngày còn lại đến ngày đáo hạn
     * > 0: Còn n ngày nữa mới đến hạn
     * = 0: Đúng ngày hôm nay đến hạn
     * < 0: Đã quá hạn |n| ngày
     */
    fun daysUntil(targetDateStr: String): Long {
        val target = parseDate(targetDateStr)
        val today = Date()

        val calTarget = Calendar.getInstance().apply {
            time = target
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calToday = Calendar.getInstance().apply {
            time = today
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = calTarget.timeInMillis - calToday.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    /**
     * Kiểm tra xem sổ tiết kiệm có sắp đến hạn không (trong vòng 15 ngày tới)
     */
    fun isExpiringSoon(maturityDateStr: String, thresholdDays: Int = 15): Boolean {
        val days = daysUntil(maturityDateStr)
        return days in 0..thresholdDays
    }

    /**
     * Chuyển đổi định dạng ngày dd/MM/yyyy sang dd/MM/yy (hiển thị thêm 2 số năm)
     * Ví dụ: 15/10/2026 -> 15/10/26
     */
    fun formatShortYearDate(dateStr: String): String {
        val parts = dateStr.split("/")
        return if (parts.size == 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = if (parts[2].length >= 2) parts[2].takeLast(2) else parts[2]
            "$day/$month/$year"
        } else {
            dateStr
        }
    }

    /**
     * Tính số tháng chênh lệch giữa 2 mốc ngày (từ ngày bắt đầu đến ngày đáo hạn).
     * Đảm bảo trả về tối thiểu 1 tháng.
     */
    fun monthsBetween(startDateStr: String, endDateStr: String): Int {
        val startDate = parseDate(startDateStr)
        val endDate = parseDate(endDateStr)
        if (endDate.time <= startDate.time) return 1

        val calStart = Calendar.getInstance().apply { time = startDate }
        val calEnd = Calendar.getInstance().apply { time = endDate }

        val yearDiff = calEnd.get(Calendar.YEAR) - calStart.get(Calendar.YEAR)
        val monthDiff = calEnd.get(Calendar.MONTH) - calStart.get(Calendar.MONTH)
        var totalMonths = yearDiff * 12 + monthDiff

        val dayDiff = calEnd.get(Calendar.DAY_OF_MONTH) - calStart.get(Calendar.DAY_OF_MONTH)
        if (dayDiff > 14) {
            totalMonths += 1
        }

        return maxOf(1, totalMonths)
    }
}
