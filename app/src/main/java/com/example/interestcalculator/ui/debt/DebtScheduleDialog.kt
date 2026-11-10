package com.example.interestcalculator.ui.debt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.interestcalculator.data.model.DebtScheduleResult
import com.example.interestcalculator.data.model.DebtSourceType
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun DebtScheduleDialog(
    customerName: String,
    result: DebtScheduleResult,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var expandedPeriodIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
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
                            text = "Lịch Trả Nợ Chi Tiết (Đa Nguồn)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (customerName.isNotBlank()) "Khách hàng: $customerName" else "Tổng hợp nghĩa vụ thanh toán hàng tháng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    Text("Kỳ", Modifier.width(34.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Text("Ngày", Modifier.width(66.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Text("Gốc", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Lãi", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Tổng trả", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Text("Dư nợ", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End)
                    Box(Modifier.width(18.dp)) // Space for expand indicator
                }

                // Table Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    itemsIndexed(result.schedule) { index, item ->
                        val isExpanded = expandedPeriodIndex == index
                        val bg = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .clickable {
                                    expandedPeriodIndex = if (isExpanded) null else index
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${item.period}", Modifier.width(34.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
                                Text(DateTimeUtils.formatShortYearDate(item.paymentDate), Modifier.width(66.dp), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.Center)
                                Text(CurrencyUtils.formatScheduleVnd(item.principalPayment), Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End)
                                Text(CurrencyUtils.formatScheduleVnd(item.interestPayment), Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End, color = Color(0xFFD32F2F))
                                Text(CurrencyUtils.formatScheduleVnd(item.totalPayment), Modifier.weight(1.1f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                                Text(CurrencyUtils.formatScheduleVnd(item.remainingBalance), Modifier.weight(1.2f), fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.End)
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp).width(18.dp)
                                )
                            }

                            // Expanded Breakdown by Organization
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Chi tiết trả nợ kỳ ${item.period} (${item.paymentDate}):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    item.itemBreakdowns.forEach { b ->
                                        val isBank = b.sourceType == DebtSourceType.BANK
                                        val badgeColor = Color(b.sourceType.colorHex)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(badgeColor, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = b.itemName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = badgeColor
                                                )
                                            }

                                            Text(
                                                text = "Gốc: ${CurrencyUtils.formatScheduleVnd(b.principalPayment)} | Lãi: ${CurrencyUtils.formatScheduleVnd(b.interestPayment)} | Còn: ${CurrencyUtils.formatScheduleVnd(b.remainingBalance)}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
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
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng dư nợ ban đầu (Gốc):", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(result.totalInitialDebt), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(" • Khối Ngân hàng (Gốc | Lãi):", fontSize = 11.sp, color = Color(DebtSourceType.BANK.colorHex))
                            Text(
                                "${CurrencyUtils.formatCompactVnd(result.bankInitialDebt)} | ${CurrencyUtils.formatCompactVnd(result.bankTotalInterest)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(DebtSourceType.BANK.colorHex)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(" • Khối Tổ chức tín dụng (Gốc | Lãi):", fontSize = 11.sp, color = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex))
                            Text(
                                "${CurrencyUtils.formatCompactVnd(result.tctdInitialDebt)} | ${CurrencyUtils.formatCompactVnd(result.tctdTotalInterest)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng tiền lãi toàn bộ:", fontSize = 12.sp)
                            Text(CurrencyUtils.formatVnd(result.totalInterest), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lãi suất bình quân gia quyền:", fontSize = 12.sp)
                            Text("${result.weightedAverageRate}%/năm", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = DividerDefaults.color.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tổng cộng cả gốc & lãi:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatVnd(result.totalPayment), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
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
                    Text("Chia sẻ báo giá & lịch trả nợ cho khách")
                }
            }
        }
    }
}
