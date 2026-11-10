package com.example.interestcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.data.model.KpiProfile
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.model.LoanStatus
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.model.SavingsStatus
import com.example.interestcalculator.util.CurrencyUtils

@Composable
fun KpiHeaderBanner(
    kpi: KpiProfile,
    loans: List<LoanProfile>,
    savings: List<SavingsProfile>,
    onUpdateKpi: (KpiProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var isMinimized by remember { mutableStateOf(true) }

    // Tính toán số liệu thực tế (chỉ tính lại khi dữ liệu thay đổi)
    val totalDisbursed = remember(loans) {
        loans.filter { it.status == LoanStatus.DISBURSED }.sumOf { it.principalAmount }
    }
    val disbursedLoanCount = remember(loans) {
        loans.count { it.status == LoanStatus.DISBURSED }
    }

    val totalSavings = remember(savings) {
        savings.filter { it.status != SavingsStatus.SETTLED }.sumOf { it.depositAmount }
    }
    val activeSavingsCount = remember(savings) {
        savings.count { it.status != SavingsStatus.SETTLED }
    }

    val expiringSavingsCount = remember(savings) {
        savings.count { it.status == SavingsStatus.EXPIRING_SOON }
    }

    val loanProgress = remember(totalDisbursed, kpi.loanDisbursementTarget) {
        if (kpi.loanDisbursementTarget > 0) {
            (totalDisbursed / kpi.loanDisbursementTarget).toFloat().coerceIn(0f, 1f)
        } else 0f
    }

    val savingsProgress = remember(totalSavings, kpi.savingsMobilizationTarget) {
        if (kpi.savingsMobilizationTarget > 0) {
            (totalSavings / kpi.savingsMobilizationTarget).toFloat().coerceIn(0f, 1f)
        } else 0f
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMinimized) 2.dp else 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isMinimized) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(if (isMinimized) 10.dp else 16.dp)
        ) {
            // Header: Banker Profile + Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMinimized = !isMinimized }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isMinimized) 32.dp else 40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(if (isMinimized) 18.dp else 24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = kpi.staffName,
                            style = if (isMinimized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = kpi.bankName,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = if (isMinimized) 11.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cài đặt KPI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isMinimized = !isMinimized },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMinimized) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isMinimized) "Mở rộng" else "Thu gọn",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Compact Mini-Summary when Minimized
            if (isMinimized) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vay: ${CurrencyUtils.formatCompactVnd(totalDisbursed)} (${(loanProgress * 100).toInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                    Text(
                        text = "Gửi: ${CurrencyUtils.formatCompactVnd(totalSavings)} (${(savingsProgress * 100).toInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    if (expiringSavingsCount > 0) {
                        Text(
                            text = "⚠️ $expiringSavingsCount sổ đáo hạn",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBF360C)
                        )
                    }
                }
            }

            // Expandable Full Details
            androidx.compose.animation.AnimatedVisibility(visible = !isMinimized) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Two KPI Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cho vay Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF0288D1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Dư nợ giải ngân",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = CurrencyUtils.formatCompactVnd(totalDisbursed),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mục tiêu: ${CurrencyUtils.formatCompactVnd(kpi.loanDisbursementTarget)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { loanProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF0288D1),
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(loanProgress * 100).toInt()}% ($disbursedLoanCount HĐ)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Tiết kiệm Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Huy động vốn",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = CurrencyUtils.formatCompactVnd(totalSavings),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Mục tiêu: ${CurrencyUtils.formatCompactVnd(kpi.savingsMobilizationTarget)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { savingsProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF2E7D32),
                                    trackColor = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(savingsProgress * 100).toInt()}% ($activeSavingsCount Sổ)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Expiry Alert Banner
                    if (expiringSavingsCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFECB3))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Có $expiringSavingsCount sổ tiết kiệm sắp đáo hạn trong 15 ngày tới! Hãy liên hệ khách hàng.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        KpiEditDialog(
            currentKpi = kpi,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onUpdateKpi(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun KpiEditDialog(
    currentKpi: KpiProfile,
    onDismiss: () -> Unit,
    onSave: (KpiProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentKpi.staffName) }
    var bank by remember { mutableStateOf(currentKpi.bankName) }
    var loanTarget by remember { mutableStateOf(currentKpi.loanDisbursementTarget) }
    var savingsTarget by remember { mutableStateOf(currentKpi.savingsMobilizationTarget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cài đặt Hồ sơ & Chỉ tiêu KPI", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên cán bộ ngân hàng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Tên ngân hàng / Chi nhánh") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                MoneyInputField(
                    label = "Chỉ tiêu giải ngân cho vay",
                    value = loanTarget,
                    onValueChange = { loanTarget = it }
                )
                MoneyInputField(
                    label = "Chỉ tiêu huy động tiết kiệm",
                    value = savingsTarget,
                    onValueChange = { savingsTarget = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentKpi.copy(
                            staffName = name.ifBlank { "Chuyên viên QHKH" },
                            bankName = bank.ifBlank { "Ngân hàng" },
                            loanDisbursementTarget = loanTarget,
                            savingsMobilizationTarget = savingsTarget
                        )
                    )
                }
            ) {
                Text("Lưu cài đặt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
