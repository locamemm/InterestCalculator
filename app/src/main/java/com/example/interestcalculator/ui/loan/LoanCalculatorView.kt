package com.example.interestcalculator.ui.loan

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.model.SavedLoanParams
import com.example.interestcalculator.data.repository.BankDataRepository
import com.example.interestcalculator.domain.LoanCalculator
import com.example.interestcalculator.ui.components.DateInputField
import com.example.interestcalculator.ui.components.LoanScheduleDialog
import com.example.interestcalculator.ui.components.MoneyInputField
import com.example.interestcalculator.ui.components.RateInputField
import com.example.interestcalculator.ui.components.SavedLoanParamsDialog
import com.example.interestcalculator.ui.components.ShareQuoteDialog
import com.example.interestcalculator.ui.components.TermInputField
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils

@Composable
fun LoanCalculatorView(
    repository: BankDataRepository,
    onSaveLoan: (LoanProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val lastParams by repository.lastLoanParams.collectAsState()
    val presets by repository.savedLoanPresets.collectAsState()
    val loans by repository.loans.collectAsState()

    var customerName by remember(lastParams.id) { mutableStateOf(lastParams.customerName) }
    var principalAmount by remember(lastParams.id) { mutableDoubleStateOf(lastParams.principalAmount) }
    var termMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.termMonths) }
    var repaymentPeriodMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.repaymentPeriodMonths) }
    var baseRate by remember(lastParams.id) { mutableDoubleStateOf(lastParams.baseRate) }
    var isBaseRateMonthly by remember(lastParams.id) { mutableStateOf(lastParams.isBaseRateMonthly) }
    var startDateStr by remember(lastParams.id) { mutableStateOf(lastParams.startDateStr) }

    // Ưu đãi
    var hasPreferential by remember(lastParams.id) { mutableStateOf(lastParams.hasPreferential) }
    var preferentialMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.preferentialMonths) }
    var preferentialRate by remember(lastParams.id) { mutableDoubleStateOf(lastParams.preferentialRate) }

    // Ân hạn
    var hasGracePeriod by remember(lastParams.id) { mutableStateOf(lastParams.hasGracePeriod) }
    var gracePeriodMonths by remember(lastParams.id) { mutableIntStateOf(lastParams.gracePeriodMonths) }

    // Phương thức
    var selectedMethod by remember(lastParams.id) { mutableStateOf(lastParams.selectedMethod) }

    // Dialogs
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedParamsDialog by remember { mutableStateOf(false) }

    // Tự động lưu thông số vừa nhập vào bộ nhớ
    LaunchedEffect(
        customerName,
        principalAmount,
        termMonths,
        repaymentPeriodMonths,
        baseRate,
        isBaseRateMonthly,
        startDateStr,
        hasPreferential,
        preferentialMonths,
        preferentialRate,
        hasGracePeriod,
        gracePeriodMonths,
        selectedMethod
    ) {
        kotlinx.coroutines.delay(300)
        repository.saveLastLoanParams(
            SavedLoanParams(
                id = lastParams.id,
                customerName = customerName,
                principalAmount = principalAmount,
                termMonths = termMonths,
                repaymentPeriodMonths = repaymentPeriodMonths,
                baseRate = baseRate,
                isBaseRateMonthly = isBaseRateMonthly,
                startDateStr = startDateStr,
                hasPreferential = hasPreferential,
                preferentialMonths = preferentialMonths,
                preferentialRate = preferentialRate,
                hasGracePeriod = hasGracePeriod,
                gracePeriodMonths = gracePeriodMonths,
                selectedMethod = selectedMethod,
                savedAt = lastParams.savedAt
            )
        )
    }

    // Tính toán kết quả
    val calculationResult by remember(
        principalAmount,
        termMonths,
        repaymentPeriodMonths,
        baseRate,
        hasPreferential,
        preferentialMonths,
        preferentialRate,
        hasGracePeriod,
        gracePeriodMonths,
        selectedMethod,
        startDateStr
    ) {
        derivedStateOf {
            LoanCalculator.calculate(
                principalAmount = principalAmount,
                termMonths = termMonths,
                baseInterestRate = baseRate,
                hasPreferential = hasPreferential,
                preferentialMonths = if (hasPreferential) preferentialMonths else 0,
                preferentialRate = if (hasPreferential) preferentialRate else baseRate,
                gracePeriodMonths = if (hasGracePeriod) gracePeriodMonths else 0,
                method = selectedMethod,
                startDateStr = startDateStr,
                repaymentPeriodMonths = repaymentPeriodMonths
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
        // Card Nhập liệu Khách hàng & Số tiền
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
                        text = "Thông Tin Khoản Vay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                MoneyInputField(
                    label = "Số tiền vay",
                    value = principalAmount,
                    onValueChange = { principalAmount = it }
                )

                // Thời hạn vay (nhập số, +/- 1, 3, 6 tháng)
                TermInputField(
                    label = "Thời hạn vay",
                    valueMonths = termMonths,
                    onValueChange = { termMonths = it },
                    minMonths = 1,
                    maxMonths = 360,
                    presets = listOf(6, 12, 24, 36, 60, 120, 180, 240, 360)
                )

                // Kỳ hạn trả nợ (nhập số, chu kỳ thanh toán)
                TermInputField(
                    label = "Kỳ hạn trả nợ",
                    valueMonths = repaymentPeriodMonths,
                    onValueChange = { repaymentPeriodMonths = it },
                    minMonths = 1,
                    maxMonths = termMonths.coerceAtLeast(1),
                    accentColor = Color(0xFF1565C0),
                    presets = listOf(1, 3, 6, 12, termMonths).distinct().filter { it <= termMonths }
                )

                // Lãi suất cơ sở (nhập số, +/- 0.1%, 0.5%, 1.0%, tùy chọn Năm hoặc 1 Tháng)
                RateInputField(
                    label = "Lãi suất cơ sở / sau ưu đãi",
                    rateValue = baseRate,
                    onRateChange = { baseRate = it },
                    allowPeriodToggle = true,
                    isMonthlyPeriod = isBaseRateMonthly,
                    onPeriodModeChange = { isBaseRateMonthly = it },
                    minRate = 0.5,
                    maxRate = 50.0,
                    presets = listOf(6.5, 7.5, 8.5, 9.5, 10.5, 11.5, 12.5)
                )

                // Ngày bắt đầu / giải ngân
                DateInputField(
                    label = "Ngày bắt đầu / giải ngân",
                    selectedDate = startDateStr,
                    onDateSelected = { startDateStr = it }
                )
            }
        }

        // Card Tùy chọn Nâng cao (Ưu đãi & Ân hạn)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ưu đãi lãi suất
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chương trình ưu đãi lãi suất", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Áp dụng lãi suất cố định thời gian đầu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasPreferential, onCheckedChange = { hasPreferential = it })
                }

                if (hasPreferential) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TermInputField(
                            label = "Số tháng ưu đãi lãi suất",
                            valueMonths = preferentialMonths,
                            onValueChange = { preferentialMonths = it },
                            minMonths = 1,
                            maxMonths = termMonths,
                            presets = listOf(3, 6, 12, 18, 24, 36)
                        )
                        RateInputField(
                            label = "Lãi suất ưu đãi (%/năm)",
                            rateValue = preferentialRate,
                            onRateChange = { preferentialRate = it },
                            minRate = 0.0,
                            maxRate = 35.0,
                            presets = listOf(4.5, 5.5, 6.5, 7.5, 8.5)
                        )
                    }
                }

                HorizontalDivider(color = DividerDefaults.color)

                // Ân hạn nợ gốc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ân hạn nợ gốc (Grace Period)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Các tháng đầu chỉ trả lãi, không trả gốc", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasGracePeriod, onCheckedChange = { hasGracePeriod = it })
                }

                if (hasGracePeriod) {
                    OutlinedTextField(
                        value = gracePeriodMonths.toString(),
                        onValueChange = { gracePeriodMonths = it.toIntOrNull() ?: 0 },
                        label = { Text("Số tháng ân hạn gốc") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                HorizontalDivider(color = DividerDefaults.color)

                // Phương thức trả nợ
                Text("Phương thức trả nợ:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LoanMethod.values().forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(method.title, fontWeight = FontWeight.SemiBold)
                                    Text(method.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Card KẾT QUẢ DỰ TOÁN FINTECH
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
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
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
                        text = "KẾT QUẢ DỰ TOÁN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlight First Month
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Số tiền trả tháng đầu:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatVnd(calculationResult.firstMonthPayment),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (calculationResult.firstMonthPayment != calculationResult.lastMonthPayment) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Tháng cuối:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatVnd(calculationResult.lastMonthPayment),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng số tiền vay (Gốc):", fontSize = 13.sp)
                    Text(CurrencyUtils.formatVnd(calculationResult.totalPrincipal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng tiền lãi phải trả:", fontSize = 13.sp)
                    Text(
                        CurrencyUtils.formatVnd(calculationResult.totalInterest),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng cộng cả gốc & lãi:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        CurrencyUtils.formatVnd(calculationResult.totalPayment),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
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
                Text("Lịch trả nợ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { showShareDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
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
        LoanScheduleDialog(
            customerName = customerName,
            schedule = calculationResult.schedule,
            totalPrincipal = calculationResult.totalPrincipal,
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
        val quoteText = LoanCalculator.generateConsultationText(
            customerName = customerName,
            principalAmount = principalAmount,
            termMonths = termMonths,
            baseRate = baseRate,
            hasPreferential = hasPreferential,
            preferentialMonths = preferentialMonths,
            preferentialRate = preferentialRate,
            method = selectedMethod,
            result = calculationResult,
            startDateStr = startDateStr,
            repaymentPeriodMonths = repaymentPeriodMonths
        )
        ShareQuoteDialog(
            title = "Bản Tư Vấn Khoản Vay (Zalo/SMS)",
            quoteText = quoteText,
            onDismiss = { showShareDialog = false }
        )
    }

    // Save Dialog
    if (showSaveDialog) {
        SaveLoanDialog(
            defaultCustomerName = customerName,
            defaultAmount = principalAmount,
            defaultTerm = termMonths,
            defaultRepaymentPeriod = repaymentPeriodMonths,
            defaultRate = baseRate,
            defaultMethod = selectedMethod,
            hasPreferential = hasPreferential,
            preferentialMonths = preferentialMonths,
            preferentialRate = preferentialRate,
            gracePeriodMonths = if (hasGracePeriod) gracePeriodMonths else 0,
            defaultDisbursementDate = startDateStr,
            isReadOnlyParams = true,
            existingLoans = loans,
            onDismiss = { showSaveDialog = false },
            onSave = { newLoan ->
                onSaveLoan(newLoan)
                showSaveDialog = false
            }
        )
    }

    // Saved Params Dialog
    if (showSavedParamsDialog) {
        SavedLoanParamsDialog(
            presets = presets,
            onSelectPreset = { preset ->
                customerName = preset.customerName
                principalAmount = preset.principalAmount
                termMonths = preset.termMonths
                repaymentPeriodMonths = preset.repaymentPeriodMonths
                baseRate = preset.baseRate
                isBaseRateMonthly = preset.isBaseRateMonthly
                startDateStr = preset.startDateStr
                hasPreferential = preset.hasPreferential
                preferentialMonths = preset.preferentialMonths
                preferentialRate = preset.preferentialRate
                hasGracePeriod = preset.hasGracePeriod
                gracePeriodMonths = preset.gracePeriodMonths
                selectedMethod = preset.selectedMethod
            },
            onDeletePreset = { presetId -> repository.deleteSavedLoanPreset(presetId) },
            onSaveCurrentAsPreset = { name ->
                repository.addSavedLoanPreset(
                    SavedLoanParams(
                        name = name,
                        customerName = customerName,
                        principalAmount = principalAmount,
                        termMonths = termMonths,
                        repaymentPeriodMonths = repaymentPeriodMonths,
                        baseRate = baseRate,
                        isBaseRateMonthly = isBaseRateMonthly,
                        startDateStr = startDateStr,
                        hasPreferential = hasPreferential,
                        preferentialMonths = preferentialMonths,
                        preferentialRate = preferentialRate,
                        hasGracePeriod = hasGracePeriod,
                        gracePeriodMonths = gracePeriodMonths,
                        selectedMethod = selectedMethod
                    )
                )
            },
            onResetToDefault = {
                val def = SavedLoanParams()
                customerName = def.customerName
                principalAmount = def.principalAmount
                termMonths = def.termMonths
                repaymentPeriodMonths = def.repaymentPeriodMonths
                baseRate = def.baseRate
                isBaseRateMonthly = def.isBaseRateMonthly
                startDateStr = DateTimeUtils.todayString()
                hasPreferential = def.hasPreferential
                preferentialMonths = def.preferentialMonths
                preferentialRate = def.preferentialRate
                hasGracePeriod = def.hasGracePeriod
                gracePeriodMonths = def.gracePeriodMonths
                selectedMethod = def.selectedMethod
            },
            onDismiss = { showSavedParamsDialog = false }
        )
    }
}
