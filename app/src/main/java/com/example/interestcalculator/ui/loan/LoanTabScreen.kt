package com.example.interestcalculator.ui.loan

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.repository.BankDataRepository
import kotlinx.coroutines.launch

@Composable
fun LoanTabScreen(
    repository: BankDataRepository,
    loans: List<LoanProfile>,
    onAddLoan: (LoanProfile) -> Unit,
    onUpdateLoan: (LoanProfile) -> Unit,
    onDeleteLoan: (String) -> Unit,
    selectedSubTab: Int = 0,
    onSubTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val subTabPagerState = rememberPagerState(
        initialPage = selectedSubTab,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedSubTab) {
        if (subTabPagerState.currentPage != selectedSubTab) {
            subTabPagerState.scrollToPage(selectedSubTab)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = {
                    onSubTabSelected(0)
                    coroutineScope.launch { subTabPagerState.scrollToPage(0) }
                },
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = if (selectedSubTab == 0) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mô Phỏng & Tư Vấn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedSubTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Tab(
                selected = selectedSubTab == 1,
                onClick = {
                    onSubTabSelected(1)
                    coroutineScope.launch { subTabPagerState.scrollToPage(1) }
                },
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = if (selectedSubTab == 1) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quản Lý Hồ Sơ (${loans.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedSubTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalPager(
            state = subTabPagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> LoanCalculatorView(
                    repository = repository,
                    onSaveLoan = { newLoan ->
                        onAddLoan(newLoan)
                        onSubTabSelected(1)
                        coroutineScope.launch { subTabPagerState.scrollToPage(1) }
                    }
                )
                1 -> LoanListView(
                    loans = loans,
                    onAddLoan = onAddLoan,
                    onUpdateLoan = onUpdateLoan,
                    onDeleteLoan = onDeleteLoan
                )
            }
        }
    }
}
