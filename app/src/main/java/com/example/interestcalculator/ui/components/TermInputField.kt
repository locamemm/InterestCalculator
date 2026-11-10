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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermInputField(
    label: String = "Thời hạn",
    valueMonths: Int,
    onValueChange: (Int) -> Unit,
    minMonths: Int = 1,
    maxMonths: Int = 360,
    accentColor: Color = Color(0xFF7B1FA2), // Tím đậm - Thời hạn / Kỳ hạn
    presets: List<Int> = emptyList(),
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf(if (valueMonths > 0) valueMonths.toString() else "") }
    var selectedStepMonths by remember { mutableIntStateOf(1) } // Bước nhảy mặc định: 1 th

    val stepOptions = listOf(
        1 to "1 th",
        3 to "3 th",
        6 to "6 th",
        12 to "12 th (1 năm)"
    )

    LaunchedEffect(valueMonths) {
        val parsed = textInput.toIntOrNull() ?: 0
        if (parsed != valueMonths) {
            textInput = if (valueMonths > 0) valueMonths.toString() else ""
        }
    }

    val years = valueMonths / 12
    val remMonths = valueMonths % 12
    val yearText = when {
        years > 0 && remMonths > 0 -> "≈ $years năm $remMonths tháng"
        years > 0 -> "≈ $years năm"
        else -> ""
    }

    val currentStepLabel = when (selectedStepMonths) {
        12 -> "1 năm"
        else -> "$selectedStepMonths th"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Direct Number Input with +/- step buttons
        OutlinedTextField(
            value = textInput,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }
                textInput = digits
                val num = digits.toIntOrNull()
                if (num != null) {
                    onValueChange(num.coerceIn(minMonths, maxMonths))
                } else {
                    onValueChange(0)
                }
            },
            label = { Text(label) },
            suffix = {
                Text(
                    text = "tháng",
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            },
            leadingIcon = {
                IconButton(
                    onClick = {
                        val next = (valueMonths - selectedStepMonths).coerceAtLeast(minMonths)
                        onValueChange(next)
                        textInput = next.toString()
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
                            onValueChange(0)
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
            supportingText = {
                if (yearText.isNotEmpty()) {
                    Text(
                        text = yearText,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
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

        Spacer(modifier = Modifier.height(4.dp))

        // Step Selector Chips (1 th, 3 th, 6 th, 12 th) - Bấm vào là cộng ngay, đồng thời chọn bước cho nút trừ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepOptions.forEach { (months, label) ->
                val isSelected = selectedStepMonths == months
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStepMonths = months
                        val next = (valueMonths + months).coerceAtMost(maxMonths)
                        textInput = next.toString()
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
