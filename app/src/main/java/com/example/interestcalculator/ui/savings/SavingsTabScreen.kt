package com.example.interestcalculator.ui.savings

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
import androidx.compose.material.icons.filled.Savings
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
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.repository.BankDataRepository
import kotlinx.coroutines.launch

@Composable
fun SavingsTabScreen(
    repository: BankDataRepository,
    savings: List<SavingsProfile>,
    onAddSavings: (SavingsProfile) -> Unit,
    onUpdateSavings: (SavingsProfile) -> Unit,
    onDeleteSavings: (String) -> Unit,
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
                        tint = if (selectedSubTab == 0) Color(0xFF2E7D32) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mô Phỏng & Lãi Suất",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedSubTab == 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
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
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (selectedSubTab == 1) Color(0xFF2E7D32) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quản Lý Sổ (${savings.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedSubTab == 1) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
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
                0 -> SavingsCalculatorView(
                    repository = repository,
                    onSaveSavings = { newSavings ->
                        onAddSavings(newSavings)
                        onSubTabSelected(1)
                        coroutineScope.launch { subTabPagerState.scrollToPage(1) }
                    }
                )
                1 -> SavingsListView(
                    savings = savings,
                    onAddSavings = onAddSavings,
                    onUpdateSavings = onUpdateSavings,
                    onDeleteSavings = onDeleteSavings
                )
            }
        }
    }
}
