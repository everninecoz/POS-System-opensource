package com.posopensrc.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.ProfitLossReport
import com.posopensrc.ui.theme.ExpenseColor
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossScreen(
    viewModel: ProfitLossViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Laba Rugi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Period Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedPeriod == "today",
                onClick = viewModel::loadTodayReport,
                label = { Text("Hari Ini") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = uiState.selectedPeriod == "week",
                onClick = viewModel::loadWeekReport,
                label = { Text("Minggu Ini") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = uiState.selectedPeriod == "month",
                onClick = viewModel::loadMonthReport,
                label = { Text("Bulan Ini") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.report != null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    ProfitLossSummary(report = uiState.report!!)
                }

                // Revenue vs Cost
                item {
                    RevenueCostCard(report = uiState.report!!)
                }

                // Profit Margin
                item {
                    ProfitMarginCard(report = uiState.report!!)
                }

                // Top Selling Products
                if (uiState.report!!.topSellingProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Produk Terlaris",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(uiState.report!!.topSellingProducts.take(5)) { product ->
                        TopSellingProductItem(product = product)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfitLossSummary(report: ProfitLossReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Pendapatan",
            value = DateTimeUtils.formatCurrencySimple(report.totalRevenue),
            icon = Icons.Default.TrendingUp,
            iconTint = IncomeColor
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Modal",
            value = DateTimeUtils.formatCurrencySimple(report.totalCost),
            icon = Icons.Default.TrendingDown,
            iconTint = ExpenseColor
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            label = "Laba Bersih",
            value = DateTimeUtils.formatCurrencySimple(report.netProfit),
            icon = if (report.netProfit >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            iconTint = if (report.netProfit >= 0) IncomeColor else ExpenseColor
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RevenueCostCard(report: ProfitLossReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Rincian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                label = "Total Pendapatan",
                value = DateTimeUtils.formatCurrencySimple(report.totalRevenue)
            )
            DetailRow(
                label = "Total Modal (Harga Beli)",
                value = DateTimeUtils.formatCurrencySimple(report.totalCost)
            )
            DetailRow(
                label = "Total Diskon",
                value = "-${DateTimeUtils.formatCurrencySimple(report.totalDiscounts)}",
                valueColor = ExpenseColor
            )
            DetailRow(
                label = "Total Pajak",
                value = DateTimeUtils.formatCurrencySimple(report.totalTax)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LABA BERSIH",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateTimeUtils.formatCurrencySimple(report.netProfit),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (report.netProfit >= 0) IncomeColor else ExpenseColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jumlah Transaksi",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${report.transactionCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rata-rata per Transaksi",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = DateTimeUtils.formatCurrencySimple(report.averageTransactionValue),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ProfitMarginCard(report: ProfitLossReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.profitMargin >= 20) IncomeColor.copy(alpha = 0.1f) else WarningColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Margin Laba",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format("%.1f", report.profitMargin)}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (report.profitMargin >= 20) IncomeColor else WarningColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    report.profitMargin >= 30 -> "Sangat Baik"
                    report.profitMargin >= 20 -> "Baik"
                    report.profitMargin >= 10 -> "Cukup"
                    report.profitMargin >= 0 -> "Rendah"
                    else -> "Rugi"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (report.profitMargin >= 20) IncomeColor else WarningColor
            )
        }
    }
}

@Composable
private fun TopSellingProductItem(product: com.posopensrc.domain.model.TopSellingProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName.ifBlank { "Produk #${product.productId}" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Terjual: ${product.totalQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeUtils.formatCurrencySimple(product.totalRevenue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Laba: ${DateTimeUtils.formatCurrencySimple(product.profit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.profit >= 0) IncomeColor else ExpenseColor
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}
