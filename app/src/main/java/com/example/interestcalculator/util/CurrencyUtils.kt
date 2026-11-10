package com.example.interestcalculator.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val formatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.GERMANY).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("#,###", symbols)
    }

    private val decimalFormatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.GERMANY).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("#,##0.0", symbols)
    }

    private val scheduleDecimalFormatter: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.GERMANY).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        DecimalFormat("#,##0.##", symbols)
    }

    fun formatVnd(amount: Double): String {
        return "${formatter.format(amount.toLong())} ₫"
    }

    fun formatVnd(amount: Long): String {
        return "${formatter.format(amount)} ₫"
    }

    fun formatNumber(amount: Double): String {
        return formatter.format(amount.toLong())
    }

    fun formatNumber(amount: Long): String {
        return formatter.format(amount)
    }

    fun formatCompactVnd(amount: Double): String {
        val abs = Math.abs(amount)
        return when {
            abs >= 1_000_000 -> {
                val tr = amount / 1_000_000.0
                if (tr % 1.0 == 0.0) "${formatter.format(tr.toLong())} triệu" else "${decimalFormatter.format(tr)} triệu"
            }
            abs >= 1_000 -> {
                val k = amount / 1_000.0
                if (k % 1.0 == 0.0) "${formatter.format(k.toLong())}k" else "${decimalFormatter.format(k)}k"
            }
            else -> "${formatter.format(amount.toLong())} ₫"
        }
    }

    /**
     * Định dạng số tiền dành riêng cho Lịch trả lãi và Lịch trả nợ.
     * Viết tắt "triệu" thành "tr" và làm tròn tối đa 2 chữ số thập phân (#,##0.##)
     * giúp hiển thị chi tiết, chính xác số tiền mà không chiếm nhiều diện tích bảng.
     */
    fun formatScheduleVnd(amount: Double): String {
        val abs = Math.abs(amount)
        return when {
            abs >= 1_000_000 -> {
                val tr = amount / 1_000_000.0
                val roundedTr = Math.round(tr)
                if (Math.abs(tr - roundedTr) < 1e-6) "${formatter.format(roundedTr)} tr" else "${scheduleDecimalFormatter.format(tr)} tr"
            }
            abs >= 1_000 -> {
                val k = amount / 1_000.0
                val roundedK = Math.round(k)
                if (Math.abs(k - roundedK) < 1e-6) "${formatter.format(roundedK)}k" else "${scheduleDecimalFormatter.format(k)}k"
            }
            else -> "${formatter.format(amount.toLong())} ₫"
        }
    }

    fun formatMillions(amount: Double): String {
        val tr = amount / 1_000_000.0
        return if (tr % 1.0 == 0.0) "${formatter.format(tr.toLong())} triệu" else "${decimalFormatter.format(tr)} triệu"
    }

    fun parseAmount(text: String): Double {
        val clean = text.replace(".", "").replace(",", "").replace(" ", "").replace("₫", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    /**
     * Đọc số tiền thành chữ tiếng Việt chuẩn ngân hàng
     */
    fun amountToWords(amount: Long): String {
        if (amount == 0L) return "Không đồng"
        var n = amount
        val units = arrayOf("", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ")
        val digits = arrayOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")

        fun readThreeDigits(number: Int, isFirstGroup: Boolean): String {
            val h = number / 100
            val t = (number % 100) / 10
            val u = number % 10
            val sb = StringBuilder()

            if (h > 0 || !isFirstGroup) {
                sb.append(digits[h]).append(" trăm ")
            }

            if (t > 1) {
                sb.append(digits[t]).append(" mươi ")
                if (u == 1) sb.append("mốt")
                else if (u == 5) sb.append("lăm")
                else if (u > 0) sb.append(digits[u])
            } else if (t == 1) {
                sb.append("mười ")
                if (u == 5) sb.append("lăm")
                else if (u > 0) sb.append(digits[u])
            } else {
                if (u > 0) {
                    if (h > 0 || !isFirstGroup) sb.append("lẻ ")
                    sb.append(digits[u])
                }
            }

            return sb.toString().trim()
        }

        val groups = mutableListOf<Int>()
        while (n > 0) {
            groups.add((n % 1000).toInt())
            n /= 1000
        }

        val result = StringBuilder()
        for (i in groups.indices.reversed()) {
            val groupVal = groups[i]
            if (groupVal > 0) {
                val groupStr = readThreeDigits(groupVal, i == groups.size - 1)
                result.append(groupStr).append(" ").append(units[i]).append(" ")
            }
        }

        val text = result.toString().trim().replace(Regex("\\s+"), " ")
        if (text.isEmpty()) return "Không đồng"
        return text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1) + " đồng"
    }
}
