package com.example.interestcalculator.ui.savings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.model.SavingsStatus
import com.example.interestcalculator.domain.SavingsCalculator
import com.example.interestcalculator.ui.components.SavingsScheduleDialog
import com.example.interestcalculator.ui.components.ShareQuoteDialog
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun SavingsListView(
    savings: List<SavingsProfile>,
    onAddSavings: (SavingsProfile) -> Unit = {},
    onUpdateSavings: (SavingsProfile) -> Unit,
    onDeleteSavings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<SavingsStatus?>(null) }

    // Dialog states
    var savingsToEdit by remember { mutableStateOf<SavingsProfile?>(null) }
    var savingsToDelete by remember { mutableStateOf<SavingsProfile?>(null) }
    var savingsScheduleToShow by remember { mutableStateOf<SavingsProfile?>(null) }
    var shareQuoteText by remember { mutableStateOf<String?>(null) }

    val filteredSavings = remember(savings, searchQuery, selectedStatusFilter) {
        savings.filter { item ->
            val matchesSearch = item.customerName.contains(searchQuery, ignoreCase = true) ||
                    item.customerPhone.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
            matchesSearch && matchesStatus
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên khách, SĐT...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            val expiringCount = savings.count { it.status == SavingsStatus.EXPIRING_SOON }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Tất cả (${savings.size})", fontSize = 12.sp) }
                )

                if (expiringCount > 0) {
                    FilterChip(
                        selected = selectedStatusFilter == SavingsStatus.EXPIRING_SOON,
                        onClick = { selectedStatusFilter = SavingsStatus.EXPIRING_SOON },
                        label = { Text("⚠️ Sắp đáo hạn ($expiringCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFE0B2),
                            selectedLabelColor = Color(0xFFE65100)
                        )
                    )
                }

                SavingsStatus.values().filter { it != SavingsStatus.EXPIRING_SOON }.forEach { status ->
                    val count = savings.count { it.status == status }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text("${status.title} ($count)", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            val totalAmount = remember(filteredSavings) { filteredSavings.sumOf { it.depositAmount } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9).copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredSavings.size} sổ tiết kiệm",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "Tổng huy động: ${CurrencyUtils.formatCompactVnd(totalAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Savings List
            if (filteredSavings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có sổ tiết kiệm nào phù hợp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSavings, key = { it.id }) { item ->
                        SavingsCardItem(
                            savings = item,
                            onScheduleClick = { savingsScheduleToShow = item },
                            onEditClick = { savingsToEdit = item },
                            onDeleteClick = { savingsToDelete = item },
                            onRenewClick = {
                                // Tái tục sổ thêm 1 kỳ hạn
                                val newDepositDate = DateTimeUtils.todayString()
                                val newMaturityDate = DateTimeUtils.addMonths(newDepositDate, item.termMonths)
                                onUpdateSavings(
                                    item.copy(
                                        depositDate = newDepositDate,
                                        maturityDate = newMaturityDate,
                                        status = SavingsStatus.ACTIVE
                                    )
                                )
                            },
                            onCallClick = {
                                if (item.customerPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.customerPhone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onSmsClick = {
                                if (item.customerPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${item.customerPhone}"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    // Edit Dialog
    if (savingsToEdit != null) {
        SaveSavingsDialog(
            initialSavings = savingsToEdit,
            existingSavings = savings,
            onDismiss = { savingsToEdit = null },
            onSave = { updated ->
                onUpdateSavings(updated)
                savingsToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (savingsToDelete != null) {
        AlertDialog(
            onDismissRequest = { savingsToDelete = null },
            title = { Text("Xác nhận xóa sổ tiết kiệm") },
            text = { Text("Bạn có chắc chắn muốn xóa sổ tiết kiệm của khách hàng '${savingsToDelete?.customerName}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        savingsToDelete?.let { onDeleteSavings(it.id) }
                        savingsToDelete = null
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { savingsToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Schedule Dialog
    if (savingsScheduleToShow != null) {
        val sav = savingsScheduleToShow!!
        val result = remember(sav) {
            SavingsCalculator.calculate(
                depositAmount = sav.depositAmount,
                termMonths = sav.termMonths,
                periodMonths = sav.periodMonths,
                annualInterestRate = sav.interestRate,
                paymentType = sav.interestPaymentType,
                isAutoRenewCompounding = sav.isAutoRenewCompounding,
                startDateStr = sav.depositDate.ifBlank { DateTimeUtils.todayString() }
            )
        }

        SavingsScheduleDialog(
            customerName = sav.customerName,
            schedule = result.schedule,
            totalPrincipal = result.principalAmount,
            totalInterest = result.totalInterest,
            onDismiss = { savingsScheduleToShow = null },
            onShare = {
                val quote = SavingsCalculator.generateConsultationText(
                    customerName = sav.customerName,
                    depositAmount = sav.depositAmount,
                    termMonths = sav.termMonths,
                    periodMonths = sav.periodMonths,
                    interestRate = sav.interestRate,
                    paymentType = sav.interestPaymentType,
                    isAutoRenewCompounding = sav.isAutoRenewCompounding,
                    result = result,
                    startDateStr = sav.depositDate.ifBlank { DateTimeUtils.todayString() }
                )
                savingsScheduleToShow = null
                shareQuoteText = quote
            }
        )
    }

    // Share Dialog
    if (shareQuoteText != null) {
        ShareQuoteDialog(
            title = "Bản Tư Vấn Gửi Tiết Kiệm (Zalo/SMS)",
            quoteText = shareQuoteText!!,
            onDismiss = { shareQuoteText = null }
        )
    }
}

@Composable
fun SavingsCardItem(
    savings: SavingsProfile,
    onScheduleClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenewClick: () -> Unit,
    onCallClick: () -> Unit,
    onSmsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpiring = savings.status == SavingsStatus.EXPIRING_SOON
    val daysRemaining = DateTimeUtils.daysUntil(savings.maturityDate)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpiring) 4.dp else 2.dp),
        border = if (isExpiring) BorderStroke(1.5.dp, Color(0xFFFFA000)) else null,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Expiring Alert Header if applicable
            if (isExpiring) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFECB3))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (daysRemaining > 0) "Sắp đáo hạn sau $daysRemaining ngày! (Đến hạn: ${savings.maturityDate})" else "Đáo hạn hôm nay (${savings.maturityDate})!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF360C)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = savings.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (savings.customerPhone.isNotBlank()) {
                        Text(
                            text = savings.customerPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(savings.status.colorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = savings.status.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(savings.status.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${savings.interestPaymentType.title}${if (savings.isAutoRenewCompounding) " (Lãi kép)" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatVnd(savings.depositAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dates & Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val periodSuffix = if (savings.periodMonths != savings.termMonths) " (Kỳ: ${savings.periodMonths}th)" else ""
                Text(
                    text = "${savings.termMonths} tháng$periodSuffix • Lãi: ${savings.interestRate}%/năm",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Đáo hạn: ${savings.maturityDate}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isExpiring) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (savings.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ghi chú: ${savings.note}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (savings.customerPhone.isNotBlank()) {
                        IconButton(onClick = onCallClick, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Gọi điện", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onSmsClick, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Message, contentDescription = "Nhắn tin", tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Quick Renew / Tái tục button
                    IconButton(onClick = onRenewClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Tái tục sổ", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onScheduleClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = "Xem lịch", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
