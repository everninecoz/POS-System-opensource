package com.posopensrc.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Transaction
import com.posopensrc.ui.components.EmptyState
import com.posopensrc.ui.components.LoadingScreen
import com.posopensrc.ui.components.SectionHeader
import com.posopensrc.ui.components.StatCard
import com.posopensrc.ui.components.StatusBadge
import com.posopensrc.ui.components.bounceClick
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSmallScreen = configuration.screenHeightDp < 480

    if (uiState.isLoading) {
        LoadingScreen(modifier = Modifier.fillMaxSize())
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isSmallScreen) 12.dp else 20.dp, vertical = if (isSmallScreen) 8.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 18.dp)
    ) {
        // Modern Cashier Banner Header
        if (!isLandscape || !isSmallScreen) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(
                                    text = "TERMINAL AKTIF",
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ringkasan Toko Hari Ini",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pantau penjualan, transaksi, dan stok kasir secara real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Primary Stats Grid
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 14.dp),
                maxItemsInEachRow = 4
            ) {
                val cardModifier = Modifier
                    .weight(1f)
                    .widthIn(min = if (isSmallScreen) 130.dp else 160.dp)
                
                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.AttachMoney,
                    value = DateTimeUtils.formatCurrencySimple(uiState.stats.todaySales),
                    label = "Penjualan",
                    iconTint = IncomeColor
                )
                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.ShoppingCart,
                    value = "${uiState.stats.todayTransactions}",
                    label = "Transaksi",
                    iconTint = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.ShoppingBag,
                    value = "${uiState.stats.totalProducts}",
                    label = "Produk",
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.Warning,
                    value = "${uiState.stats.lowStockCount}",
                    label = "Stok Tipis",
                    iconTint = WarningColor
                )
            }
        }

        // Secondary Period Stats (Mingguan & Bulanan)
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = 2
            ) {
                val cardModifier = Modifier
                    .weight(1f)
                    .widthIn(min = 220.dp)

                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.DateRange,
                    value = DateTimeUtils.formatCurrencySimple(uiState.stats.weekSales),
                    label = "Penjualan Minggu Ini",
                    iconTint = IncomeColor
                )
                StatCard(
                    modifier = cardModifier,
                    icon = Icons.Default.DateRange,
                    value = DateTimeUtils.formatCurrencySimple(uiState.stats.monthSales),
                    label = "Penjualan Bulan Ini",
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Recent Transactions Section Header
        item {
            SectionHeader(
                title = "Transaksi Terakhir",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    title = "Belum Ada Transaksi",
                    message = "Semua transaksi kasir yang selesai hari ini akan muncul di sini."
                )
            }
        } else {
            items(uiState.recentTransactions) { transaction ->
                ModernTransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
private fun ModernTransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = transaction.invoiceNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateTimeUtils.formatTime(transaction.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = transaction.paymentMethod.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = "+${DateTimeUtils.formatCurrencySimple(transaction.total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = IncomeColor
            )
        }
    }
}
