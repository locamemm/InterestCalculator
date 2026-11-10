package com.example.interestcalculator.ui.loan

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
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.model.LoanStatus
import com.example.interestcalculator.ui.components.DateInputField
import com.example.interestcalculator.ui.components.MoneyInputField
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveLoanDialog(
    initialLoan: LoanProfile? = null,
    defaultCustomerName: String = "",
    defaultAmount: Double = 500_000_000.0,
    defaultTerm: Int = 12,
    defaultRepaymentPeriod: Int = 1,
    defaultRate: Double = 9.0,
    defaultMethod: LoanMethod = LoanMethod.REDUCING_BALANCE,
    hasPreferential: Boolean = false,
    preferentialMonths: Int = 0,
    preferentialRate: Double = defaultRate,
    gracePeriodMonths: Int = 0,
    defaultDisbursementDate: String = DateTimeUtils.todayString(),
    isReadOnlyParams: Boolean = false,
    existingLoans: List<LoanProfile> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (LoanProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialLoan?.customerName ?: defaultCustomerName.ifBlank { "" }) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf(initialLoan?.customerPhone ?: "") }
    var purpose by remember { mutableStateOf(initialLoan?.loanPurpose ?: "Vay mua nhà đất") }
    var amount by remember { mutableStateOf(initialLoan?.principalAmount ?: defaultAmount) }
    var term by remember { mutableStateOf((initialLoan?.termMonths ?: defaultTerm).toString()) }
    var repaymentPeriod by remember { mutableStateOf((initialLoan?.repaymentPeriodMonths ?: defaultRepaymentPeriod).toString()) }
    var rate by remember { mutableStateOf((initialLoan?.interestRate ?: defaultRate).toString()) }
    var disbursementDate by remember { mutableStateOf(initialLoan?.disbursementDate?.ifBlank { defaultDisbursementDate } ?: defaultDisbursementDate) }
    var method by remember { mutableStateOf(initialLoan?.calculationMethod ?: defaultMethod) }
    var status by remember { mutableStateOf(initialLoan?.status ?: LoanStatus.CONSULTING) }
    var note by remember { mutableStateOf(initialLoan?.note ?: "") }

    var methodExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialLoan == null) "Lưu Hồ Sơ Khoản Vay" else "Chỉnh Sửa Hồ Sơ Khoản Vay",
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

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Mục đích vay (Nhà đất, Ô tô, KD...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isReadOnlyParams) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Thông số gói vay đã thiết lập",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
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
                                Text("Số tiền vay:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.formatVnd(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Thời hạn vay:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$term tháng", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kỳ hạn trả nợ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$repaymentPeriod tháng/kỳ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lãi suất vay:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$rate%/năm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ngày giải ngân:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(disbursementDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Phương thức:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(method.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (hasPreferential) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ưu đãi lãi suất:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$preferentialRate%/năm ($preferentialMonths tháng đầu)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (gracePeriodMonths > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ân hạn gốc:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$gracePeriodMonths tháng", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else {
                    MoneyInputField(
                        label = "Số tiền vay",
                        value = amount,
                        onValueChange = { amount = it }
                    )

                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("Thời hạn vay (tháng)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = repaymentPeriod,
                        onValueChange = { repaymentPeriod = it },
                        label = { Text("Kỳ hạn trả nợ (tháng/kỳ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = it },
                        label = { Text("Lãi suất (%/năm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DateInputField(
                        label = "Ngày bắt đầu / giải ngân",
                        selectedDate = disbursementDate,
                        onDateSelected = { disbursementDate = it }
                    )

                    // Dropdown Method
                    ExposedDropdownMenuBox(
                        expanded = methodExpanded,
                        onExpandedChange = { methodExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = method.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Phương thức trả nợ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            LoanMethod.values().forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        method = item
                                        methodExpanded = false
                                    }
                                )
                            }
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
                        label = { Text("Trạng thái hồ sơ") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        LoanStatus.values().forEach { item ->
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
                    label = { Text("Ghi chú hồ sơ") },
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
                        existingLoans.any { loan ->
                            loan.id != initialLoan?.id &&
                            loan.customerName.trim().equals(trimmedName, ignoreCase = true)
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
                    val parsedRepaymentPeriod = repaymentPeriod.filter { it.isDigit() }.toIntOrNull() ?: 1
                    val parsedRate = rate.replace(",", ".").toDoubleOrNull() ?: 9.0
                    val loan = (initialLoan ?: LoanProfile(
                        customerName = trimmedName,
                        principalAmount = amount,
                        termMonths = parsedTerm,
                        repaymentPeriodMonths = parsedRepaymentPeriod,
                        interestRate = parsedRate
                    )).copy(
                        customerName = trimmedName,
                        customerPhone = phone.trim(),
                        loanPurpose = purpose.trim(),
                        principalAmount = amount,
                        termMonths = parsedTerm,
                        repaymentPeriodMonths = parsedRepaymentPeriod,
                        interestRate = parsedRate,
                        calculationMethod = method,
                        status = status,
                        hasPreferential = hasPreferential,
                        preferentialMonths = preferentialMonths,
                        preferentialRate = preferentialRate,
                        gracePeriodMonths = gracePeriodMonths,
                        disbursementDate = disbursementDate.ifBlank { DateTimeUtils.todayString() },
                        note = note.trim()
                    )
                    onSave(loan)
                }
            ) {
                Text("Lưu hồ sơ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
