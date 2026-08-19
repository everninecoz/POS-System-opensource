package com.posopensrc.ui.screens.reports

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf("today") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSmallScreen = configuration.screenHeightDp < 480

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isSmallScreen) 8.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 16.dp)
    ) {
        if (!isLandscape || !isSmallScreen) {
            item {
                Text(
                    text = "Laporan Penjualan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Period Selection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPeriod == "today",
                    onClick = {
                        selectedPeriod = "today"
                        viewModel.loadTodayReport()
                    },
                    label = { Text("Hari Ini", fontSize = if (isSmallScreen) 12.sp else 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = selectedPeriod == "week",
                    onClick = {
                        selectedPeriod = "week"
                        viewModel.loadWeekReport()
                    },
                    label = { Text("Minggu Ini", fontSize = if (isSmallScreen) 12.sp else 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = selectedPeriod == "month",
                    onClick = {
                        selectedPeriod = "month"
                        viewModel.loadMonthReport()
                    },
                    label = { Text("Bulan Ini", fontSize = if (isSmallScreen) 12.sp else 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Stats Cards
        uiState.report?.let { report ->
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = if (isLandscape) 3 else 2
                ) {
                    val cardModifier = Modifier.weight(1f).widthIn(min = 120.dp)
                    StatCard(
                        modifier = cardModifier,
                        title = "Penjualan",
                        value = DateTimeUtils.formatCurrencySimple(report.totalSales),
                        compact = isSmallScreen
                    )
                    StatCard(
                        modifier = cardModifier,
                        title = "Transaksi",
                        value = "${report.totalTransactions}",
                        compact = isSmallScreen
                    )
                    StatCard(
                        modifier = cardModifier,
                        title = "Rata-rata",
                        value = DateTimeUtils.formatCurrencySimple(report.averageTransaction),
                        compact = isSmallScreen
                    )
                }
            }

            // Payment Method Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isSmallScreen) 10.dp else 16.dp)
                    ) {
                        Text(
                            text = "Metode Pembayaran",
                            style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PaymentStatItem("Tunai", report.cashSales, compact = isSmallScreen)
                            PaymentStatItem("QRIS", report.qrisSales, compact = isSmallScreen)
                            PaymentStatItem("Transfer", report.transferSales, compact = isSmallScreen)
                        }
                    }
                }
            }

            // Recent Transactions Header
            item {
                Text(
                    text = "Detail Transaksi",
                    style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ada transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isSmallScreen) 8.dp else 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = transaction.invoiceNumber,
                                    style = if (isSmallScreen) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = DateTimeUtils.formatDateTime(transaction.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = DateTimeUtils.formatCurrencySimple(transaction.total),
                                style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = IncomeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    compact: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 16.dp)
        ) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PaymentStatItem(
    method: String,
    amount: Double,
    compact: Boolean = false
) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = DateTimeUtils.formatCurrencySimple(amount),
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
