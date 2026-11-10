package com.example.interestcalculator.ui.debt

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.interestcalculator.data.model.DebtSourceItem
import com.example.interestcalculator.data.model.DebtSourceType
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.SavedDebtParams
import com.example.interestcalculator.data.repository.BankDataRepository
import com.example.interestcalculator.domain.DebtCalculator
import com.example.interestcalculator.ui.components.DateInputField
import com.example.interestcalculator.ui.components.MoneyInputField
import com.example.interestcalculator.ui.components.RateInputField
import com.example.interestcalculator.ui.components.ShareQuoteDialog
import com.example.interestcalculator.util.CurrencyUtils
import com.example.interestcalculator.util.DateTimeUtils
import java.util.UUID

@Composable
fun DebtCalculatorView(
    repository: BankDataRepository,
    modifier: Modifier = Modifier
) {
    val lastParams by repository.lastDebtParams.collectAsState()

    var customerName by remember(lastParams.id) { mutableStateOf(lastParams.customerName) }
    var startDateStr by remember(lastParams.id) { mutableStateOf(lastParams.startDateStr.ifBlank { DateTimeUtils.todayString() }) }
    var bankRate by remember(lastParams.id) { mutableDoubleStateOf(lastParams.bankRate) }
    var isBankRateMonthly by remember(lastParams.id) { mutableStateOf(lastParams.isBankRateMonthly) }
    var tctdRate by remember(lastParams.id) { mutableDoubleStateOf(lastParams.tctdRate) }
    var isTctdRateMonthly by remember(lastParams.id) { mutableStateOf(lastParams.isTctdRateMonthly) }
    var selectedMethod by remember(lastParams.id) { mutableStateOf(lastParams.calculationMethod) }
    var items by remember(lastParams.id) { mutableStateOf(lastParams.items) }

    // Dialogs
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Tự động lưu thông số sau mỗi lần chỉnh sửa (debounce 300ms)
    LaunchedEffect(
        customerName,
        startDateStr,
        bankRate,
        isBankRateMonthly,
        tctdRate,
        isTctdRateMonthly,
        selectedMethod,
        items
    ) {
        kotlinx.coroutines.delay(300)
        repository.saveLastDebtParams(
            SavedDebtParams(
                id = lastParams.id,
                customerName = customerName,
                startDateStr = startDateStr,
                bankRate = bankRate,
                isBankRateMonthly = isBankRateMonthly,
                tctdRate = tctdRate,
                isTctdRateMonthly = isTctdRateMonthly,
                calculationMethod = selectedMethod,
                items = items,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    // Tính toán kết quả dòng tiền đa nguồn
    val calculationResult by remember(
        items,
        startDateStr,
        bankRate,
        tctdRate,
        selectedMethod
    ) {
        derivedStateOf {
            DebtCalculator.calculate(
                items = items,
                startDateStr = startDateStr,
                bankRate = bankRate,
                tctdRate = tctdRate,
                method = selectedMethod
            )
        }
    }

    val bankQuickPresets = listOf("Vietcombank", "BIDV", "VietinBank", "Techcombank", "MB Bank", "VPBank", "ACB", "TPBank")
    val tctdQuickPresets = listOf("FE Credit", "Home Credit", "HD Saison", "Mirae Asset", "Shinhan Finance", "Mcredit")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CARD 1: CẤU HÌNH CHUNG & MỐC THỜI GIAN
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
                    Column {
                        Text(
                            text = "Mốc Thời Gian & Lãi Suất Riêng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Chọn ngày bắt đầu và thiết lập 2 mức lãi suất",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AssistChip(
                        onClick = {
                            startDateStr = DateTimeUtils.todayString()
                            bankRate = 10.5
                            tctdRate = 28.0
                            selectedMethod = LoanMethod.REDUCING_BALANCE
                        },
                        label = { Text("Mặc định", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Tên khách hàng / Hồ sơ nợ (Tùy chọn)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 1. Mốc thời gian hiện tại
                DateInputField(
                    label = "Mốc thời gian hiện tại (Ngày tính toán)",
                    selectedDate = startDateStr,
                    onDateSelected = { startDateStr = it }
                )

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.6f))

                // 2. Hai ô lãi suất riêng biệt: Ngân hàng & Tổ chức tín dụng
                Text(
                    text = "Lãi suất theo từng khối tổ chức:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Lãi suất Ngân Hàng
                RateInputField(
                    label = "Lãi suất Ngân Hàng (%/năm)",
                    rateValue = bankRate,
                    onRateChange = { bankRate = it },
                    accentColor = Color(DebtSourceType.BANK.colorHex),
                    allowPeriodToggle = true,
                    isMonthlyPeriod = isBankRateMonthly,
                    onPeriodModeChange = { isBankRateMonthly = it },
                    minRate = 1.0,
                    maxRate = 35.0,
                    presets = listOf(7.5, 8.5, 9.5, 10.5, 11.5, 12.5)
                )

                // Lãi suất Tổ chức tín dụng
                RateInputField(
                    label = "Lãi suất Tổ Chức Tín Dụng (%/năm)",
                    rateValue = tctdRate,
                    onRateChange = { tctdRate = it },
                    accentColor = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex),
                    allowPeriodToggle = true,
                    isMonthlyPeriod = isTctdRateMonthly,
                    onPeriodModeChange = { isTctdRateMonthly = it },
                    minRate = 5.0,
                    maxRate = 70.0,
                    presets = listOf(18.0, 24.0, 28.0, 32.0, 36.0, 42.0)
                )

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.6f))

                // Phương thức trả nợ
                Text("Phương thức tính dòng tiền trả:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        LoanMethod.REDUCING_BALANCE to "Dư nợ giảm dần (Gốc chia đều, chuẩn NH)",
                        LoanMethod.ANNUITY to "Trả góp đều (Gốc + Lãi cố định)",
                        LoanMethod.END_OF_TERM to "Lãi hàng tháng, Gốc tất toán khi đáo hạn"
                    ).forEach { (method, desc) ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = {
                                Text(desc, fontSize = 12.sp, fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // CARD 2: DANH SÁCH CÁC NGUỒN DƯ NỢ
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Danh Sách Tổ Chức Nợ (${items.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Thêm các khoản nợ từ Ngân Hàng hoặc TCTD",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // DẤU + ĐỂ THÊM NGÂN HÀNG HOẶC TỔ CHỨC TÍN DỤNG (TÁCH 2 NÚT RIÊNG RÕ RÀNG)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val newItem = DebtSourceItem(
                                id = UUID.randomUUID().toString(),
                                name = "Ngân hàng ${items.count { it.sourceType == DebtSourceType.BANK } + 1}",
                                sourceType = DebtSourceType.BANK,
                                currentDebt = 100_000_000.0,
                                maturityDate = DateTimeUtils.addMonths(startDateStr, 24)
                            )
                            items = items + newItem
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(DebtSourceType.BANK.colorHex)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm Ngân Hàng", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newItem = DebtSourceItem(
                                id = UUID.randomUUID().toString(),
                                name = "Tổ chức tín dụng ${items.count { it.sourceType == DebtSourceType.CREDIT_INSTITUTION } + 1}",
                                sourceType = DebtSourceType.CREDIT_INSTITUTION,
                                currentDebt = 30_000_000.0,
                                maturityDate = DateTimeUtils.addMonths(startDateStr, 12)
                            )
                            items = items + newItem
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm TCTD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Gợi ý nhanh danh sách tổ chức
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gợi ý thêm nhanh:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bankQuickPresets.take(4).forEach { name ->
                            AssistChip(
                                onClick = {
                                    items = items + DebtSourceItem(
                                        name = name,
                                        sourceType = DebtSourceType.BANK,
                                        currentDebt = 100_000_000.0,
                                        maturityDate = DateTimeUtils.addMonths(startDateStr, 24)
                                    )
                                },
                                label = { Text("+ $name", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(DebtSourceType.BANK.colorHex).copy(alpha = 0.12f))
                            )
                        }
                        tctdQuickPresets.take(3).forEach { name ->
                            AssistChip(
                                onClick = {
                                    items = items + DebtSourceItem(
                                        name = name,
                                        sourceType = DebtSourceType.CREDIT_INSTITUTION,
                                        currentDebt = 30_000_000.0,
                                        maturityDate = DateTimeUtils.addMonths(startDateStr, 12)
                                    )
                                },
                                label = { Text("+ $name", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex).copy(alpha = 0.12f))
                            )
                        }
                    }
                }

                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f))

                // Danh sách từng thẻ tổ chức nợ
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có khoản nợ nào. Nhấn nút + ở trên để thêm Ngân Hàng hoặc Tổ Chức Tín Dụng.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items.forEachIndexed { index, item ->
                        val isBank = item.sourceType == DebtSourceType.BANK
                        val cardAccentColor = Color(item.sourceType.colorHex)
                        val monthsLeft = DateTimeUtils.monthsBetween(
                            startDateStr,
                            if (item.maturityDate.isNotBlank()) item.maturityDate else DateTimeUtils.addMonths(startDateStr, 12)
                        )
                        val effectiveRate = item.customRate ?: if (isBank) bankRate else tctdRate

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardAccentColor.copy(alpha = 0.05f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardAccentColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header của thẻ: Badge loại, số thứ tự, nút xóa
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = cardAccentColor,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = item.sourceType.shortTitle,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "#${index + 1}. ${if (isBank) "Ngân hàng" else "Tổ chức tín dụng"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = cardAccentColor
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            color = cardAccentColor.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "$effectiveRate%/năm",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = cardAccentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            items = items.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Xóa",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // 1. Tên tổ chức
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { newName ->
                                        items = items.mapIndexed { i, itm -> if (i == index) itm.copy(name = newName) else itm }
                                    },
                                    label = { Text("Tên tổ chức / Ngân hàng") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 2. Dư nợ hiện tại
                                MoneyInputField(
                                    label = "Dư nợ hiện tại",
                                    value = item.currentDebt,
                                    onValueChange = { newDebt ->
                                        items = items.mapIndexed { i, itm -> if (i == index) itm.copy(currentDebt = newDebt) else itm }
                                    },
                                    accentColor = cardAccentColor
                                )

                                // 3. Ngày đáo hạn (riêng của mỗi tổ chức)
                                val currentMaturity = if (item.maturityDate.isNotBlank()) item.maturityDate else DateTimeUtils.addMonths(startDateStr, 12)
                                DateInputField(
                                    label = "Ngày đáo hạn (Còn $monthsLeft tháng)",
                                    selectedDate = currentMaturity,
                                    onDateSelected = { newDate ->
                                        items = items.mapIndexed { i, itm -> if (i == index) itm.copy(maturityDate = newDate) else itm }
                                    },
                                    accentColor = cardAccentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // CARD 3: KẾT QUẢ DỰ TOÁN DƯ NỢ HÀNG THÁNG
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
                        text = "KẾT QUẢ DỰ TOÁN DƯ NỢ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${calculationResult.totalMonths} tháng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlight First Month Payment
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

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Lãi suất bình quân:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${calculationResult.weightedAverageRate}%/năm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng dư nợ hiện tại (Gốc):", fontSize = 13.sp)
                    Text(CurrencyUtils.formatVnd(calculationResult.totalInitialDebt), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                if (calculationResult.bankInitialDebt > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(" • Dư nợ khối Ngân Hàng:", fontSize = 12.sp, color = Color(DebtSourceType.BANK.colorHex))
                        Text(CurrencyUtils.formatVnd(calculationResult.bankInitialDebt), fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(DebtSourceType.BANK.colorHex))
                    }
                }

                if (calculationResult.tctdInitialDebt > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(" • Dư nợ khối Tổ chức tín dụng:", fontSize = 12.sp, color = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex))
                        Text(CurrencyUtils.formatVnd(calculationResult.tctdInitialDebt), fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color(DebtSourceType.CREDIT_INSTITUTION.colorHex))
                    }
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

        // Action Buttons Row: Lịch trả nợ & Báo giá Zalo
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

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Schedule Dialog
    if (showScheduleDialog) {
        DebtScheduleDialog(
            customerName = customerName,
            result = calculationResult,
            onDismiss = { showScheduleDialog = false },
            onShare = {
                showScheduleDialog = false
                showShareDialog = true
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        val quoteText = DebtCalculator.generateDebtConsultationText(
            customerName = customerName,
            startDateStr = startDateStr,
            bankRate = bankRate,
            tctdRate = tctdRate,
            items = items,
            result = calculationResult
        )
        ShareQuoteDialog(
            title = "Bản Tư Vấn Dư Nợ Đa Nguồn (Zalo/SMS)",
            quoteText = quoteText,
            onDismiss = { showShareDialog = false }
        )
    }
}
