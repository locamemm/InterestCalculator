package com.example.interestcalculator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    label: String = "Ngày bắt đầu",
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { onDateSelected(it) },
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = accentColor
                )
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Chọn ngày trên lịch",
                        tint = accentColor
                    )
                }
            },
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.65f),
                focusedLabelColor = accentColor,
                unfocusedLabelColor = accentColor.copy(alpha = 0.85f),
                focusedContainerColor = accentColor.copy(alpha = 0.025f),
                unfocusedContainerColor = accentColor.copy(alpha = 0.025f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Preset Chips: Hôm nay, Ngày mai, Đầu tháng sau, Ngày 15, Ngày 25
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val today = DateTimeUtils.todayString()
            val tomorrow = DateTimeUtils.tomorrowString()
            val firstOfNextMonth = DateTimeUtils.firstOfNextMonthString()

            AssistChip(
                onClick = { onDateSelected(today) },
                label = {
                    Text(
                        "Hôm nay ($today)",
                        fontSize = 11.sp,
                        fontWeight = if (selectedDate == today) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedDate == today) accentColor.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            AssistChip(
                onClick = { onDateSelected(tomorrow) },
                label = {
                    Text(
                        "Ngày mai",
                        fontSize = 11.sp,
                        fontWeight = if (selectedDate == tomorrow) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedDate == tomorrow) accentColor.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            AssistChip(
                onClick = { onDateSelected(firstOfNextMonth) },
                label = {
                    Text(
                        "Đầu tháng sau ($firstOfNextMonth)",
                        fontSize = 11.sp,
                        fontWeight = if (selectedDate == firstOfNextMonth) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedDate == firstOfNextMonth) accentColor.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.toMillis(selectedDate)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            onDateSelected(DateTimeUtils.formatMillis(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Chọn", fontWeight = FontWeight.Bold, color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
