package com.example.interestcalculator.ui.savings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.interestcalculator.data.model.SavedSavingsParams
import com.example.interestcalculator.data.model.SavingsPaymentType
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.repository.BankDataRepository
import com.example.interestcalculator.domain.SavingsCalculator
import com.example.interestcalculator.ui.components.DateInputField
import com.example.interestcalculator.ui.components.MoneyInputField
import com.example.interestcalculator.ui.components.SavedSavingsParamsDialog
import com.example.interestcalculator.ui.components.SavingsScheduleDialog
import com.example.interestcalculator.ui.components.ShareQuoteDialog
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun SavingsCalculatorView(
    repository: BankDataRepository,
    onSaveSavings: (SavingsProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val lastParams by repository.lastSavingsParams.collectAsState()
    val presets by repository.savedSavingsPresets.collectAsState()
    val savings by repository.savings.collectAsState()

    var customerName by remember(lastParams.id) { mutableStateOf(lastParams.customerName) }
    var depositAmount by remember(lastParams.id) { mutableDoubleStateOf(lastParams.depositAmount) }
    var termMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.termMonths) }
    var periodMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.periodMonths) }
    var interestRate by remember(lastParams.id) { mutableDoubleStateOf(lastParams.interestRate) }
    var startDateStr by remember(lastParams.id) { mutableStateOf(lastParams.startDateStr) }
    var paymentType by remember(lastParams.id) { mutableStateOf(lastParams.paymentType) }
    var isCompounding by remember(lastParams.id) { mutableStateOf(lastParams.isCompounding) }

    // Dialogs
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedParamsDialog by remember { mutableStateOf(false) }

    // Tự động lưu thông số vừa nhập vào bộ nhớ
    LaunchedEffect(
        customerName,
        depositAmount,
        termMonths,
        periodMonths,
        interestRate,
        startDateStr,
        paymentType,
        isCompounding
    ) {
        kotlinx.coroutines.delay(300)
        repository.saveLastSavingsParams(
            SavedSavingsParams(
                id = lastParams.id,
                customerName = customerName,
                depositAmount = depositAmount,
                termMonths = termMonths,
                periodMonths = periodMonths,
                interestRate = interestRate,
                startDateStr = startDateStr,
                paymentType = paymentType,
                isCompounding = isCompounding,
                savedAt = lastParams.savedAt
            )
        )
    }

    val calculationResult by remember(
        depositAmount,
        termMonths,
        periodMonths,
        interestRate,
        paymentType,
        isCompounding,
        startDateStr
    ) {
        derivedStateOf {
            SavingsCalculator.calculate(
                depositAmount = depositAmount,
                termMonths = termMonths,
                periodMonths = periodMonths,
                annualInterestRate = interestRate,
                paymentType = paymentType,
                isAutoRenewCompounding = isCompounding,
                startDateStr = startDateStr
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card Nhập Liệu
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thông Tin Tiền Gửi Tiết Kiệm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )

                    AssistChip(
                        onClick = { showSavedParamsDialog = true },
                        label = { Text("Thông số đã lưu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Tên khách hàng (Tùy chọn)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                MoneyInputField(
                    label = "Số tiền gửi tiết kiệm",
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    accentColor = Color(0xFF2E7D32)
                )

                // Thời hạn gửi tiết kiệm (tổng thời gian)
                com.example.interestcalculator.ui.components.TermInputField(
                    label = "Thời hạn gửi",
                    valueMonths = termMonths,
                    onValueChange = { termMonths = it },
                    minMonths = 1,
                    maxMonths = 120,
                    accentColor = Color(0xFF7B1FA2),
                    presets = listOf(1, 3, 6, 9, 12, 18, 24, 36)
                )

                // Kỳ hạn gửi (kỳ hạn sổ/chu kỳ nhận lãi)
                com.example.interestcalculator.ui.components.TermInputField(
                    label = "Kỳ hạn",
                    valueMonths = periodMonths,
                    onValueChange = { periodMonths = it },
                    minMonths = 1,
                    maxMonths = termMonths.coerceAtLeast(1),
                    accentColor = Color(0xFF1976D2),
                    presets = listOf(1, 2, 3, 6, 9, 12, termMonths).distinct().filter { it <= termMonths }
                )

                // Lãi suất huy động (nhập số, +/- 0.1%, 0.5%, 1.0%)
                com.example.interestcalculator.ui.components.RateInputField(
                    label = "Lãi suất huy động",
                    rateValue = interestRate,
                    onRateChange = { interestRate = it },
                    minRate = 0.5,
                    maxRate = 25.0,
                    accentColor = Color(0xFFD32F2F),
                    presets = listOf(3.5, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0)
                )

                // Ngày gửi tiền
                DateInputField(
                    label = "Ngày gửi tiền",
                    selectedDate = startDateStr,
                    onDateSelected = { startDateStr = it },
                    accentColor = Color(0xFF2E7D32)
                )

                // Hình thức nhận lãi
                Column {
                    Text("Hình thức trả lãi:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SavingsPaymentType.values().forEach { type ->
                            FilterChip(
                                selected = paymentType == type,
                                onClick = { paymentType = type },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(type.title, fontWeight = FontWeight.SemiBold)
                                        Text(type.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (paymentType == SavingsPaymentType.END_OF_TERM) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F5E9).copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isCompounding,
                            onCheckedChange = { isCompounding = it }
                        )
                        Text(
                            text = "Tự động quay vòng nhập gốc (Lãi kép)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Card KẾT QUẢ TIẾT KIỆM FINTECH
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LỢI NHUẬN TIẾT KIỆM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlight Total Interest
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.10f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Tổng tiền lãi thực nhận:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+ ${CurrencyUtils.formatVnd(calculationResult.totalInterest)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Số tiền gửi (Gốc ban đầu):", fontSize = 13.sp)
                    Text(CurrencyUtils.formatVnd(calculationResult.principalAmount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (paymentType == SavingsPaymentType.MONTHLY) {
                    val monthly = if (termMonths > 0) calculationResult.totalInterest / termMonths else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiền lãi nhận đều mỗi tháng:", fontSize = 13.sp)
                        Text(CurrencyUtils.formatVnd(monthly), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng nhận khi tất toán:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        CurrencyUtils.formatVnd(calculationResult.totalMaturityAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showScheduleDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lịch trả lãi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { showShareDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Báo giá Zalo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Save to Portfolio Button
        Button(
            onClick = { showSaveDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lưu vào danh sách quản lý", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Schedule Dialog
    if (showScheduleDialog) {
        SavingsScheduleDialog(
            customerName = customerName,
            schedule = calculationResult.schedule,
            totalPrincipal = calculationResult.principalAmount,
            totalInterest = calculationResult.totalInterest,
            onDismiss = { showScheduleDialog = false },
            onShare = {
                showScheduleDialog = false
                showShareDialog = true
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        val quoteText = SavingsCalculator.generateConsultationText(
            customerName = customerName,
            depositAmount = depositAmount,
            termMonths = termMonths,
            interestRate = interestRate,
            paymentType = paymentType,
            isAutoRenewCompounding = isCompounding,
            result = calculationResult,
            startDateStr = startDateStr,
            periodMonths = periodMonths
        )
        ShareQuoteDialog(
            title = "Bản Tư Vấn Gửi Tiết Kiệm (Zalo/SMS)",
            quoteText = quoteText,
            onDismiss = { showShareDialog = false }
        )
    }

    // Save Dialog
    if (showSaveDialog) {
        SaveSavingsDialog(
            defaultCustomerName = customerName,
            defaultAmount = depositAmount,
            defaultTerm = termMonths,
            defaultPeriod = periodMonths,
            defaultRate = interestRate,
            defaultPaymentType = paymentType,
            defaultIsCompounding = isCompounding,
            defaultDepositDate = startDateStr,
            isReadOnlyParams = true,
            existingSavings = savings,
            onDismiss = { showSaveDialog = false },
            onSave = { newSav ->
                onSaveSavings(newSav)
                showSaveDialog = false
            }
        )
    }

    // Saved Params Dialog
    if (showSavedParamsDialog) {
        SavedSavingsParamsDialog(
            presets = presets,
            onSelectPreset = { preset ->
                customerName = preset.customerName
                depositAmount = preset.depositAmount
                termMonths = preset.termMonths
                periodMonths = preset.periodMonths
                interestRate = preset.interestRate
                startDateStr = preset.startDateStr
                paymentType = preset.paymentType
                isCompounding = preset.isCompounding
            },
            onDeletePreset = { presetId -> repository.deleteSavedSavingsPreset(presetId) },
            onSaveCurrentAsPreset = { name ->
                repository.addSavedSavingsPreset(
                    SavedSavingsParams(
                        name = name,
                        customerName = customerName,
                        depositAmount = depositAmount,
                        termMonths = termMonths,
                        periodMonths = periodMonths,
                        interestRate = interestRate,
                        startDateStr = startDateStr,
                        paymentType = paymentType,
                        isCompounding = isCompounding
                    )
                )
            },
            onResetToDefault = {
                val def = SavedSavingsParams()
                customerName = def.customerName
                depositAmount = def.depositAmount
                termMonths = def.termMonths
                periodMonths = def.periodMonths
                interestRate = def.interestRate
                startDateStr = DateTimeUtils.todayString()
                paymentType = def.paymentType
                isCompounding = def.isCompounding
            },
            onDismiss = { showSavedParamsDialog = false }
        )
    }
}
