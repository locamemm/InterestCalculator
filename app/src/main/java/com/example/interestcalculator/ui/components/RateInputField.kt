package com.example.interestcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun RateInputField(
    label: String = "Lãi suất (%/năm)",
    rateValue: Double,
    onRateChange: (Double) -> Unit,
    minRate: Double = 0.0,
    maxRate: Double = 35.0,
    accentColor: Color = Color(0xFFD32F2F), // Đỏ ruby - Lãi suất %
    presets: List<Double> = emptyList(),
    allowPeriodToggle: Boolean = false, // Cho phép chuyển đổi giữa Năm và 1 Tháng
    isMonthlyPeriod: Boolean = false,
    onPeriodModeChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var localIsMonthly by remember { mutableStateOf(isMonthlyPeriod) }
    val isMonthly = if (onPeriodModeChange != null) isMonthlyPeriod else localIsMonthly
    val setMonthly: (Boolean) -> Unit = { monthly ->
        localIsMonthly = monthly
        onPeriodModeChange?.invoke(monthly)
    }

    val currentDisplayValue = if (isMonthly) rateValue / 12.0 else rateValue
    var textInput by remember { mutableStateOf(if (currentDisplayValue > 0) formatRateString(currentDisplayValue) else "") }
    var selectedStepRate by remember { mutableDoubleStateOf(0.1) } // Bước nhảy mặc định: 0.1%

    val stepOptions = if (isMonthly) {
        listOf(
            0.05 to "0.05%",
            0.1 to "0.1%",
            0.2 to "0.2%",
            0.5 to "0.5%"
        )
    } else {
        listOf(
            0.1 to "0.1%",
            0.2 to "0.2%",
            0.5 to "0.5%",
            1.0 to "1.0%"
        )
    }

    // Sync from outside only if numeric value changed significantly
    LaunchedEffect(rateValue, isMonthly) {
        val target = if (isMonthly) rateValue / 12.0 else rateValue
        val parsed = textInput.replace(",", ".").toDoubleOrNull()
        if (parsed == null) {
            if (target > 0) {
                textInput = formatRateString(target)
            }
        } else if (Math.abs(parsed - target) > 0.001) {
            textInput = if (target > 0) formatRateString(target) else ""
        }
    }

    val currentStepLabel = "${String.format(Locale.US, "%.2f", selectedStepRate).trimEnd('0').trimEnd('.')}%"

    Column(modifier = modifier.fillMaxWidth()) {
        // Tùy chọn chuyển đổi Lãi suất Năm / 1 Tháng (thiết kế thanh gạt nhỏ gọn, tinh tế)
        if (allowPeriodToggle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lãi suất cơ sở",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Segmented Toggle nhỏ gọn, bo tròn mềm mại
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isMonthly) accentColor else Color.Transparent)
                            .clickable {
                                if (isMonthly) {
                                    setMonthly(false)
                                    selectedStepRate = 0.1
                                    textInput = if (rateValue > 0) formatRateString(rateValue) else ""
                                }
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%/năm",
                            fontSize = 11.sp,
                            fontWeight = if (!isMonthly) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isMonthly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMonthly) accentColor else Color.Transparent)
                            .clickable {
                                if (!isMonthly) {
                                    setMonthly(true)
                                    selectedStepRate = 0.1
                                    val monthly = rateValue / 12.0
                                    textInput = if (monthly > 0) formatRateString(monthly) else ""
                                }
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%/tháng",
                            fontSize = 11.sp,
                            fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMonthly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
        }

        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                val normalized = filtered.replace(',', '.')

                val dotCount = normalized.count { it == '.' }
                if (dotCount <= 1) {
                    textInput = filtered
                    if (filtered.isEmpty()) {
                        onRateChange(0.0)
                    } else {
                        val parsed = normalized.toDoubleOrNull()
                        if (parsed != null) {
                            if (isMonthly) {
                                if (parsed in 0.0..10.0) {
                                    val annual = parsed * 12.0
                                    onRateChange(Math.round(annual * 100.0) / 100.0)
                                }
                            } else {
                                if (parsed in minRate..maxRate) {
                                    onRateChange(Math.round(parsed * 100.0) / 100.0)
                                }
                            }
                        }
                    }
                }
            },
            label = {
                Text(
                    if (allowPeriodToggle) {
                        if (isMonthly) "Lãi suất cơ sở (theo 1 tháng)" else "Lãi suất cơ sở / sau ưu đãi"
                    } else {
                        label
                    }
                )
            },
            suffix = {
                Text(
                    text = if (isMonthly) "%/tháng" else "%/năm",
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = if (allowPeriodToggle) {
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val newMonthly = !isMonthly
                                setMonthly(newMonthly)
                                selectedStepRate = 0.1
                                val target = if (newMonthly) rateValue / 12.0 else rateValue
                                textInput = if (target > 0) formatRateString(target) else ""
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    } else Modifier
                )
            },
            supportingText = {
                if (rateValue > 0) {
                    if (isMonthly) {
                        Text(
                            text = "≈ ${formatEquivalentRate(rateValue)}%/năm (Quy đổi 1 tháng × 12)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (allowPeriodToggle) {
                        Text(
                            text = "≈ ${formatEquivalentRate(rateValue / 12.0)}%/tháng (Quy đổi 1 năm = 12 tháng)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            leadingIcon = {
                IconButton(
                    onClick = {
                        if (isMonthly) {
                            val currentMonthly = rateValue / 12.0
                            val nextMonthly = (currentMonthly - selectedStepRate).coerceAtLeast(0.0)
                            val roundedMonthly = Math.round(nextMonthly * 100.0) / 100.0
                            val annual = Math.round(roundedMonthly * 12.0 * 100.0) / 100.0
                            onRateChange(annual)
                            textInput = formatRateString(roundedMonthly)
                        } else {
                            val next = Math.round((rateValue - selectedStepRate) * 100.0) / 100.0
                            val clamped = next.coerceAtLeast(minRate)
                            onRateChange(clamped)
                            textInput = formatRateString(clamped)
                        }
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
                if (textInput.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textInput = ""
                            onRateChange(0.0)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

        Spacer(modifier = Modifier.height(4.dp))

        // Step Selector Chips - Bấm vào là cộng ngay, đồng thời chọn bước cho nút trừ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepOptions.forEach { (stepRate, label) ->
                val isSelected = Math.abs(selectedStepRate - stepRate) < 0.001
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStepRate = stepRate
                        if (isMonthly) {
                            val currentMonthly = rateValue / 12.0
                            val nextMonthly = (currentMonthly + stepRate).coerceAtMost(10.0)
                            val roundedMonthly = Math.round(nextMonthly * 100.0) / 100.0
                            val annual = Math.round(roundedMonthly * 12.0 * 100.0) / 100.0
                            textInput = formatRateString(roundedMonthly)
                            onRateChange(annual)
                        } else {
                            val next = Math.round((rateValue + stepRate) * 100.0) / 100.0
                            val clamped = next.coerceAtMost(maxRate)
                            textInput = formatRateString(clamped)
                            onRateChange(clamped)
                        }
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

private fun formatRateString(rate: Double): String {
    if (rate <= 0.0) return ""
    val rounded = Math.round(rate * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        "${rounded.toLong()}"
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
}

private fun formatEquivalentRate(rate: Double): String {
    val rounded = Math.round(rate * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        "${rounded.toLong()}"
    } else {
        String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
}
