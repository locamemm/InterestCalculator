package com.example.interestcalculator.ui.loan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.model.LoanStatus
import com.example.interestcalculator.domain.LoanCalculator
import com.example.interestcalculator.ui.components.LoanScheduleDialog
import com.example.interestcalculator.ui.components.ShareQuoteDialog
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun LoanListView(
    loans: List<LoanProfile>,
    onAddLoan: (LoanProfile) -> Unit = {},
    onUpdateLoan: (LoanProfile) -> Unit,
    onDeleteLoan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<LoanStatus?>(null) }

    // Dialog states
    var loanToEdit by remember { mutableStateOf<LoanProfile?>(null) }
    var loanToDelete by remember { mutableStateOf<LoanProfile?>(null) }
    var loanScheduleToShow by remember { mutableStateOf<LoanProfile?>(null) }
    var shareQuoteText by remember { mutableStateOf<String?>(null) }

    val filteredLoans = remember(loans, searchQuery, selectedStatusFilter) {
        loans.filter { loan ->
            val matchesSearch = loan.customerName.contains(searchQuery, ignoreCase = true) ||
                    loan.customerPhone.contains(searchQuery, ignoreCase = true) ||
                    loan.loanPurpose.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == null || loan.status == selectedStatusFilter
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
                placeholder = { Text("Tìm theo tên, SĐT, mục đích vay...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("Tất cả (${loans.size})", fontSize = 12.sp) }
                )
                LoanStatus.values().forEach { status ->
                    val count = loans.count { it.status == status }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text("${status.title} ($count)", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            val totalAmount = remember(filteredLoans) { filteredLoans.sumOf { it.principalAmount } }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredLoans.size} hồ sơ vay",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tổng: ${CurrencyUtils.formatCompactVnd(totalAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Loan List
            if (filteredLoans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có hồ sơ khoản vay nào phù hợp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoans, key = { it.id }) { loan ->
                        LoanCardItem(
                            loan = loan,
                            onScheduleClick = { loanScheduleToShow = loan },
                            onEditClick = { loanToEdit = loan },
                            onDeleteClick = { loanToDelete = loan },
                            onCallClick = {
                                if (loan.customerPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${loan.customerPhone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onSmsClick = {
                                if (loan.customerPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${loan.customerPhone}"))
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
    if (loanToEdit != null) {
        SaveLoanDialog(
            initialLoan = loanToEdit,
            existingLoans = loans,
            onDismiss = { loanToEdit = null },
            onSave = { updated ->
                onUpdateLoan(updated)
                loanToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (loanToDelete != null) {
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("Xác nhận xóa hồ sơ") },
            text = { Text("Bạn có chắc chắn muốn xóa hồ sơ khoản vay của khách hàng '${loanToDelete?.customerName}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        loanToDelete?.let { onDeleteLoan(it.id) }
                        loanToDelete = null
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // View Schedule Dialog
    if (loanScheduleToShow != null) {
        val loan = loanScheduleToShow!!
        val result = remember(loan) {
            LoanCalculator.calculate(
                principalAmount = loan.principalAmount,
                termMonths = loan.termMonths,
                baseInterestRate = loan.interestRate,
                hasPreferential = loan.hasPreferential,
                preferentialMonths = loan.preferentialMonths,
                preferentialRate = loan.preferentialRate,
                gracePeriodMonths = loan.gracePeriodMonths,
                method = loan.calculationMethod,
                startDateStr = loan.disbursementDate.ifBlank { com.example.interestcalculator.util.DateTimeUtils.todayString() },
                repaymentPeriodMonths = loan.repaymentPeriodMonths
            )
        }

        LoanScheduleDialog(
            customerName = loan.customerName,
            schedule = result.schedule,
            totalPrincipal = result.totalPrincipal,
            totalInterest = result.totalInterest,
            onDismiss = { loanScheduleToShow = null },
            onShare = {
                val quote = LoanCalculator.generateConsultationText(
                    customerName = loan.customerName,
                    principalAmount = loan.principalAmount,
                    termMonths = loan.termMonths,
                    baseRate = loan.interestRate,
                    hasPreferential = loan.hasPreferential,
                    preferentialMonths = loan.preferentialMonths,
                    preferentialRate = loan.preferentialRate,
                    method = loan.calculationMethod,
                    result = result,
                    startDateStr = loan.disbursementDate.ifBlank { com.example.interestcalculator.util.DateTimeUtils.todayString() },
                    repaymentPeriodMonths = loan.repaymentPeriodMonths
                )
                loanScheduleToShow = null
                shareQuoteText = quote
            }
        )
    }

    // Share Dialog
    if (shareQuoteText != null) {
        ShareQuoteDialog(
            title = "Bản Tư Vấn Khoản Vay (Zalo/SMS)",
            quoteText = shareQuoteText!!,
            onDismiss = { shareQuoteText = null }
        )
    }
}

@Composable
fun LoanCardItem(
    loan: LoanProfile,
    onScheduleClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: () -> Unit,
    onSmsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Customer Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (loan.customerPhone.isNotBlank()) {
                        Text(
                            text = loan.customerPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(loan.status.colorHex).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = loan.status.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(loan.status.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Purpose & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loan.loanPurpose,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatVnd(loan.principalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Loan Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val periodSuffix = if (loan.repaymentPeriodMonths > 1) " • Kỳ: ${loan.repaymentPeriodMonths} th" else ""
                Text(
                    text = "${loan.termMonths} tháng (${loan.termMonths / 12} năm)$periodSuffix • Lãi: ${loan.interestRate}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = loan.calculationMethod.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (loan.disbursementDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Giải ngân: ${loan.disbursementDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Đáo hạn: ${DateTimeUtils.getPeriodDate(loan.disbursementDate, loan.termMonths)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (loan.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ghi chú: ${loan.note}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (loan.customerPhone.isNotBlank()) {
                        IconButton(onClick = onCallClick, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Gọi điện", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onSmsClick, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Message, contentDescription = "Nhắn tin", tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onScheduleClick, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = "Xem lịch", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
