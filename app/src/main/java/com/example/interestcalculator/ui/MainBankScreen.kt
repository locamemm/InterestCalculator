package com.example.interestcalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.data.repository.BankDataRepository
import com.example.interestcalculator.ui.components.KpiHeaderBanner
import com.example.interestcalculator.ui.debt.DebtCalculatorView
import com.example.interestcalculator.ui.loan.LoanTabScreen
import com.example.interestcalculator.ui.savings.SavingsTabScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBankScreen(
    repository: BankDataRepository,
    modifier: Modifier = Modifier
) {
    val loans by repository.loans.collectAsState()
    val savings by repository.savings.collectAsState()
    val kpi by repository.kpi.collectAsState()

    var mainTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val mainPagerState = rememberPagerState(
        initialPage = mainTabIndex,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mainTabIndex) {
        if (mainPagerState.currentPage != mainTabIndex) {
            mainPagerState.scrollToPage(mainTabIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TRỢ LÝ NGÂN HÀNG",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tín Dụng & Huy Động Tiết Kiệm",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // KPI & Performance Banner
            KpiHeaderBanner(
                kpi = kpi,
                loans = loans,
                savings = savings,
                onUpdateKpi = { updated -> repository.updateKpi(updated) }
            )

            // Main Tabs: 1. CHO VAY, 2. GỬI TIẾT KIỆM (Cấu trúc ngang)
            PrimaryTabRow(
                selectedTabIndex = mainTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = mainTabIndex == 0,
                    onClick = {
                        mainTabIndex = 0
                        coroutineScope.launch { mainPagerState.scrollToPage(0) }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Cho vay",
                            tint = if (mainTabIndex == 0) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHO VAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (mainTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Tab(
                    selected = mainTabIndex == 1,
                    onClick = {
                        mainTabIndex = 1
                        coroutineScope.launch { mainPagerState.scrollToPage(1) }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Gửi tiết kiệm",
                            tint = if (mainTabIndex == 1) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIẾT KIỆM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (mainTabIndex == 1) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Tab(
                    selected = mainTabIndex == 2,
                    onClick = {
                        mainTabIndex = 2
                        coroutineScope.launch { mainPagerState.scrollToPage(2) }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Tính dư nợ",
                            tint = if (mainTabIndex == 2) Color(0xFFE65100) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TÍNH DƯ NỢ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (mainTabIndex == 2) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Content giữ các màn hình trong bộ nhớ, chuyển đổi tức thì không lag
            HorizontalPager(
                state = mainPagerState,
                userScrollEnabled = false,
                beyondViewportPageCount = 2,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> LoanTabScreen(
                        repository = repository,
                        loans = loans,
                        onAddLoan = { repository.addLoan(it) },
                        onUpdateLoan = { repository.updateLoan(it) },
                        onDeleteLoan = { repository.deleteLoan(it) },
                        selectedSubTab = selectedSubTabIndex,
                        onSubTabSelected = { selectedSubTabIndex = it }
                    )
                    1 -> SavingsTabScreen(
                        repository = repository,
                        savings = savings,
                        onAddSavings = { repository.addSavings(it) },
                        onUpdateSavings = { repository.updateSavings(it) },
                        onDeleteSavings = { repository.deleteSavings(it) },
                        selectedSubTab = selectedSubTabIndex,
                        onSubTabSelected = { selectedSubTabIndex = it }
                    )
                    2 -> DebtCalculatorView(
                        repository = repository
                    )
                }
            }
        }
    }
}
