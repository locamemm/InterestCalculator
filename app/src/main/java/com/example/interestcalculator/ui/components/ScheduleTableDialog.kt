package com.example.interestcalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.interestcalculator.data.model.RepaymentScheduleItem
import com.example.interestcalculator.data.model.SavingsScheduleItem
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun LoanScheduleDialog(
    customerName: String,
    schedule: List<RepaymentScheduleItem>,
    totalPrincipal: Double,
    totalInterest: Double,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lịch Trả Nợ Chi Tiết",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (customerName.isNotBlank()) {
                            Text(
                                text = "Khách hàng: $customerName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kỳ", Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Text("Ngày", Modifier.width(68.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Text("Gốc", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Lãi", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Tổng trả", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Dư nợ", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                }

                // Table Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    itemsIndexed(schedule) { index, item ->
                        val bg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${item.period}", Modifier.width(36.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text(DateTimeUtils.formatShortYearDate(item.paymentDate), Modifier.width(68.dp), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.Center)
                            Text(CurrencyUtils.formatScheduleVnd(item.principalPayment), Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End)
                            Text(CurrencyUtils.formatScheduleVnd(item.interestPayment), Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End, color = Color(0xFFD32F2F))
                            Text(CurrencyUtils.formatScheduleVnd(item.totalPayment), Modifier.weight(1.1f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                            Text(CurrencyUtils.formatScheduleVnd(item.remainingBalance), Modifier.weight(1.2f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDefaults.color)

                // Table Footer Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng tiền vay (Gốc):", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(totalPrincipal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng tiền lãi:", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(totalInterest), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng cộng cả gốc & lãi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatVnd(totalPrincipal + totalInterest), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share Button
                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chia sẻ bảng tính cho khách hàng")
                }
            }
        }
    }
}

@Composable
fun SavingsScheduleDialog(
    customerName: String,
    schedule: List<SavingsScheduleItem>,
    totalPrincipal: Double,
    totalInterest: Double,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lịch Trả Lãi Tiết Kiệm",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        if (customerName.isNotBlank()) {
                            Text(
                                text = "Khách hàng: $customerName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFE8F5E9),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kỳ", Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text("Ngày nhận", Modifier.width(86.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text("Tiền lãi", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                    Text("Gốc nhận", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
                }

                // Table Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    itemsIndexed(schedule) { index, item ->
                        val bg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${item.period}", Modifier.width(36.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(item.payoutDate, Modifier.width(86.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(CurrencyUtils.formatScheduleVnd(item.interestAmount), Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.End, color = Color(0xFF2E7D32))
                            Text(if (item.principalAmount > 0) CurrencyUtils.formatScheduleVnd(item.principalAmount) else "-", Modifier.weight(1.2f), fontSize = 12.sp, textAlign = TextAlign.End)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerDefaults.color)

                // Table Footer Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Số tiền gửi (Gốc):", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(totalPrincipal), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng tiền lãi thực nhận:", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(totalInterest), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng nhận khi đáo hạn:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatVnd(totalPrincipal + totalInterest), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share Button
                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chia sẻ phương án gửi tiết kiệm")
                }
            }
        }
    }
}
