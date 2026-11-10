package com.example.interestcalculator.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.util.CurrencyUtils

/**
 * VisualTransformation tự động định dạng dấu chấm phân cách hàng nghìn (1.000.000)
 * mà không can thiệp vào chuỗi ký tự gốc bên dưới, giúp con trỏ chuột gõ/xóa tự nhiên,
 * không bao giờ bị thụt lùi hay giật lag.
 */
class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val totalLength = originalText.length
        val formattedBuilder = StringBuilder()
        for (i in 0 until totalLength) {
            formattedBuilder.append(originalText[i])
            val digitsFromEnd = totalLength - 1 - i
            if (digitsFromEnd > 0 && digitsFromEnd % 3 == 0) {
                formattedBuilder.append('.')
            }
        }
        val formattedText = formattedBuilder.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, totalLength)
                if (clampedOffset == 0) return 0
                val digitsFromEnd = totalLength - clampedOffset
                val dotsToRight = if (digitsFromEnd > 0) (digitsFromEnd - 1) / 3 else 0
                val totalDots = if (totalLength > 0) (totalLength - 1) / 3 else 0
                val dotsToLeft = totalDots - dotsToRight
                return (clampedOffset + dotsToLeft).coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, formattedText.length)
                var digitsCount = 0
                for (i in 0 until clampedOffset) {
                    if (formattedText[i].isDigit()) {
                        digitsCount++
                    }
                }
                return digitsCount.coerceIn(0, totalLength)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@Composable
fun MoneyInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF1976D2), // Xanh dương đậm - Tiền tệ / Vốn
    quickChips: List<Long> = emptyList()
) {
    var rawDigits by remember { mutableStateOf(if (value > 0) value.toLong().toString() else "") }
    var selectedStepAmount by remember { mutableDoubleStateOf(100_000_000.0) } // Bước nhảy mặc định: 100 triệu

    val stepOptions = listOf(
        10_000_000.0 to "10 triệu",
        50_000_000.0 to "50 triệu",
        100_000_000.0 to "100 triệu",
        500_000_000.0 to "500 triệu",
        1_000_000_000.0 to "1.000 triệu"
    )

    LaunchedEffect(value) {
        val currentParsed = rawDigits.toDoubleOrNull() ?: 0.0
        if (Math.abs(currentParsed - value) > 1.0) {
            rawDigits = if (value > 0) value.toLong().toString() else ""
        }
    }

    val millionsText = if (value >= 1_000_000) CurrencyUtils.formatMillions(value) else ""
    val words = if (value > 0) CurrencyUtils.amountToWords(value.toLong()) else ""
    val currentStepLabel = CurrencyUtils.formatCompactVnd(selectedStepAmount)

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = rawDigits,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                rawDigits = digitsOnly
                if (digitsOnly.isEmpty()) {
                    onValueChange(0.0)
                } else {
                    val num = digitsOnly.toDoubleOrNull() ?: 0.0
                    onValueChange(num)
                }
            },
            visualTransformation = remember { ThousandsSeparatorVisualTransformation() },
            label = { Text(label) },
            suffix = {
                Text(
                    text = "VNĐ",
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            },
            leadingIcon = {
                IconButton(
                    onClick = {
                        val next = (value - selectedStepAmount).coerceAtLeast(0.0)
                        rawDigits = if (next > 0) next.toLong().toString() else ""
                        onValueChange(next)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Trừ $currentStepLabel",
                        tint = Color(0xFFD32F2F)
                    )
                }
            },
            trailingIcon = {
                if (rawDigits.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            rawDigits = ""
                            onValueChange(0.0)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Xóa",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.85f),
                focusedLabelColor = accentColor,
                unfocusedLabelColor = accentColor.copy(alpha = 0.90f),
                focusedContainerColor = accentColor.copy(alpha = 0.035f),
                unfocusedContainerColor = accentColor.copy(alpha = 0.035f),
                cursorColor = accentColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Quy đổi triệu & Đọc số thành chữ
        if (value >= 1_000_000) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "≈ $millionsText",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (words.isNotEmpty()) {
                    Text(
                        text = " ($words)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Step Selector Chips (10 tr, 50 tr, 100 tr, 500 tr, 1.000 tr) - Bấm vào là cộng ngay, đồng thời chọn bước cho nút trừ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepOptions.forEach { (stepAmount, label) ->
                val isSelected = Math.abs(selectedStepAmount - stepAmount) < 1.0
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStepAmount = stepAmount
                        val next = value + stepAmount
                        rawDigits = if (next > 0) next.toLong().toString() else ""
                        onValueChange(next)
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.20f),
                        selectedLabelColor = accentColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
