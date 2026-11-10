package com.example.interestcalculator.ui.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.interestcalculator.data.model.SavingsPaymentType
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.model.SavingsStatus
import com.example.interestcalculator.ui.components.DateInputField
import com.example.interestcalculator.ui.components.MoneyInputField
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveSavingsDialog(
    initialSavings: SavingsProfile? = null,
    defaultCustomerName: String = "",
    defaultAmount: Double = 100_000_000.0,
    defaultTerm: Int = 12,
    defaultPeriod: Int = 6,
    defaultRate: Double = 5.5,
    defaultPaymentType: SavingsPaymentType = SavingsPaymentType.END_OF_TERM,
    defaultIsCompounding: Boolean = false,
    defaultDepositDate: String = DateTimeUtils.todayString(),
    isReadOnlyParams: Boolean = false,
    existingSavings: List<SavingsProfile> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (SavingsProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialSavings?.customerName ?: defaultCustomerName.ifBlank { "" }) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf(initialSavings?.customerPhone ?: "") }
    var amount by remember { mutableStateOf(initialSavings?.depositAmount ?: defaultAmount) }
    var term by remember { mutableStateOf((initialSavings?.termMonths ?: defaultTerm).toString()) }
    var period by remember { mutableStateOf((initialSavings?.periodMonths ?: defaultPeriod).toString()) }
    var rate by remember { mutableStateOf((initialSavings?.interestRate ?: defaultRate).toString()) }
    var depositDate by remember { mutableStateOf(initialSavings?.depositDate?.ifBlank { defaultDepositDate } ?: defaultDepositDate) }
    var paymentType by remember { mutableStateOf(initialSavings?.interestPaymentType ?: defaultPaymentType) }
    var isCompounding by remember { mutableStateOf(initialSavings?.isAutoRenewCompounding ?: defaultIsCompounding) }
    var status by remember { mutableStateOf(initialSavings?.status ?: SavingsStatus.ACTIVE) }
    var note by remember { mutableStateOf(initialSavings?.note ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialSavings == null) "Lưu Sổ Tiết Kiệm Khách Hàng" else "Chỉnh Sửa Sổ Tiết Kiệm",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Họ tên khách hàng *") },
                    isError = nameError != null,
                    supportingText = if (nameError != null) {
                        {
                            Text(
                                text = nameError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại liên hệ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isReadOnlyParams) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF1F8E9)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Thông số sổ tiết kiệm đã thiết lập",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            HorizontalDivider(
                                color = DividerDefaults.color.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Số tiền gửi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.formatVnd(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Thời hạn gửi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$term tháng", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kỳ hạn:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$period tháng", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lãi suất huy động:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$rate%/năm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ngày gửi tiền:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(depositDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hình thức nhận lãi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(paymentType.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (paymentType == SavingsPaymentType.END_OF_TERM) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tự động quay vòng:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (isCompounding) "Có (Lãi kép)" else "Không", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else {
                    MoneyInputField(
                        label = "Số tiền gửi tiết kiệm",
                        value = amount,
                        onValueChange = { amount = it },
                        accentColor = Color(0xFF2E7D32)
                    )

                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("Thời hạn gửi (tháng)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Kỳ hạn (tháng)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it },
                        label = { Text("Lãi suất huy động (%/năm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DateInputField(
                        label = "Ngày gửi tiền",
                        selectedDate = depositDate,
                        onDateSelected = { depositDate = it },
                        accentColor = Color(0xFF2E7D32)
                    )

                    // Dropdown Type
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = paymentType.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hình thức nhận lãi") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            SavingsPaymentType.values().forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        paymentType = item
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (paymentType == SavingsPaymentType.END_OF_TERM) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isCompounding,
                                onCheckedChange = { isCompounding = it }
                            )
                            Text("Tự động quay vòng nhập gốc (Lãi kép)")
                        }
                    }
                }

                // Dropdown Status
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trạng thái sổ") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        SavingsStatus.values().forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.title) },
                                onClick = {
                                    status = item
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú sổ tiết kiệm") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    val isDuplicate = if (trimmedName.isNotBlank()) {
                        existingSavings.any { sav ->
                            sav.id != initialSavings?.id &&
                            sav.customerName.trim().equals(trimmedName, ignoreCase = true)
                        }
                    } else false

                    if (trimmedName.isBlank()) {
                        nameError = "*Vui lòng nhập họ tên khách hàng"
                        return@Button
                    } else if (isDuplicate) {
                        nameError = "*Trùng tên với hồ sơ đã có"
                        return@Button
                    }

                    val parsedTerm = term.filter { it.isDigit() }.toIntOrNull() ?: 12
                    val parsedPeriod = period.filter { it.isDigit() }.toIntOrNull() ?: parsedTerm
                    val parsedRate = rate.replace(",", ".").toDoubleOrNull() ?: 5.5
                    val today = DateTimeUtils.todayString()
                    val finalDepositDate = depositDate.ifBlank { today }
                    val maturityDate = DateTimeUtils.addMonths(finalDepositDate, parsedTerm)

                    val updatedStatus = if (status != SavingsStatus.SETTLED && DateTimeUtils.isExpiringSoon(maturityDate, 15)) {
                        SavingsStatus.EXPIRING_SOON
                    } else status

                    val profile = (initialSavings ?: SavingsProfile(
                        customerName = trimmedName,
                        depositAmount = amount,
                        termMonths = parsedTerm,
                        periodMonths = parsedPeriod,
                        interestRate = parsedRate
                    )).copy(
                        customerName = trimmedName,
                        customerPhone = phone.trim(),
                        depositAmount = amount,
                        termMonths = parsedTerm,
                        periodMonths = parsedPeriod,
                        interestRate = parsedRate,
                        interestPaymentType = paymentType,
                        isAutoRenewCompounding = isCompounding,
                        depositDate = finalDepositDate,
                        maturityDate = maturityDate,
                        status = updatedStatus,
                        note = note.trim()
                    )
                    onSave(profile)
                }
            ) {
                Text("Lưu sổ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
