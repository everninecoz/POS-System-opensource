package com.posopensrc.ui.screens.pos

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.CartItem
import com.posopensrc.domain.model.Product
import com.posopensrc.ui.components.EmptyState
import com.posopensrc.ui.components.StatusBadge
import com.posopensrc.ui.components.bounceClick
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.StockLowBadge
import com.posopensrc.ui.theme.StockLowText
import com.posopensrc.ui.theme.StockOutBadge
import com.posopensrc.ui.theme.StockOutText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
    val showReceiptDialog by viewModel.showReceiptDialog.collectAsState()
    val lastTransaction by viewModel.lastTransaction.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSmallScreen = configuration.screenWidthDp < 600

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isSmallScreen) 8.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 16.dp)
        ) {
            // Left Panel - Products Catalog
            Column(
                modifier = Modifier.weight(0.6f)
            ) {
                PosProductsContent(uiState, viewModel, compact = isSmallScreen)
            }

            // Right Panel - Cart & Billing
            Column(
                modifier = Modifier.weight(0.4f)
            ) {
                PosCartContent(cart, viewModel, uiState, compact = isSmallScreen)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isSmallScreen) 8.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 16.dp)
        ) {
            // Top Panel - Products Catalog
            Box(modifier = Modifier.weight(0.55f)) {
                PosProductsContent(uiState, viewModel, compact = isSmallScreen)
            }

            // Bottom Panel - Cart & Billing
            Box(modifier = Modifier.weight(0.45f)) {
                PosCartContent(cart, viewModel, uiState, compact = isSmallScreen)
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            total = viewModel.getTotal(),
            onDismiss = viewModel::hidePaymentDialog,
            onPayment = { method, amount ->
                viewModel.processPayment(method, amount)
            }
        )
    }

    // Receipt Dialog
    if (showReceiptDialog && lastTransaction != null) {
        ReceiptDialog(
            transaction = lastTransaction!!,
            onDismiss = viewModel::hideReceiptDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PosProductsContent(
    uiState: PosUiState,
    viewModel: PosViewModel,
    compact: Boolean = false
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Scan Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if (compact) "Cari..." else "Cari produk atau barcode...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 18.dp else 24.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(if (compact) 10.dp else 14.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = if (compact) 14.sp else 16.sp)
        )

        Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

        // Category Filter Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { viewModel.onCategorySelected(null) },
                label = { Text("Semua", fontSize = if (compact) 12.sp else 14.sp) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) },
                    label = { Text(category, fontSize = if (compact) 12.sp else 14.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

        // Products Grid
        val displayProducts = if (uiState.filteredProducts.isEmpty() && uiState.searchQuery.isEmpty()) {
            uiState.products
        } else {
            uiState.filteredProducts
        }

        if (displayProducts.isEmpty()) {
            EmptyState(
                title = "Produk Tidak Ditemukan",
                message = "Coba ubah kata kunci pencarian atau kategori."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayProducts, key = { it.id }) { product ->
                    ProductQuickAddCard(
                        product = product,
                        onClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PosCartContent(
    cart: List<CartItem>,
    viewModel: PosViewModel,
    uiState: PosUiState,
    compact: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 2.dp else 3.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 8.dp else 16.dp)
        ) {
            // Cart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                    )
                    Text(
                        text = if (compact) "Keranjang" else "Keranjang Kasir",
                        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusBadge(
                    text = "${cart.sumOf { it.quantity }}",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    compact = compact
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 10.dp))

            // Cart Item List
            if (cart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (compact) "Kosong" else "Keranjang masih kosong\nPilih produk dari daftar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
                ) {
                    items(cart, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            compact = compact,
                            onIncrease = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.updateQuantity(item.product.id, 1)
                            },
                            onDecrease = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.updateQuantity(item.product.id, -1)
                            },
                            onRemove = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.removeFromCart(item.product.id)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 10.dp))

            // Summary & Checkout
            Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)) {
                // Financial Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(if (compact) 8.dp else 14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 6.dp else 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SummaryRow(
                            label = "TOTAL",
                            value = DateTimeUtils.formatCurrencySimple(viewModel.getTotal()),
                            isBold = true,
                            compact = compact
                        )
                    }
                }

                // Checkout Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearCart()
                        },
                        modifier = Modifier
                            .weight(0.3f)
                            .height(if (compact) 36.dp else 48.dp)
                            .bounceClick(),
                        contentPadding = if (compact) androidx.compose.foundation.layout.PaddingValues(0.dp) else ButtonDefaults.ContentPadding,
                        shape = RoundedCornerShape(8.dp),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text(if (compact) "CLR" else "Reset", fontWeight = FontWeight.SemiBold, fontSize = if (compact) 11.sp else 14.sp)
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showPaymentDialog()
                        },
                        modifier = Modifier
                            .weight(0.7f)
                            .height(if (compact) 36.dp else 48.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text(
                            text = "BAYAR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (compact) 13.sp else 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductQuickAddCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.94f, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Product Monogram Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (product.category?.firstOrNull() ?: product.name.firstOrNull() ?: 'P').uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = DateTimeUtils.formatCurrencySimple(product.price),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = IncomeColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stock Status
            when {
                product.stock <= 0 -> {
                    StatusBadge(
                        text = "Habis",
                        containerColor = StockOutBadge,
                        contentColor = StockOutText
                    )
                }
                product.isLowStock -> {
                    StatusBadge(
                        text = "Sisa ${product.stock}",
                        containerColor = StockLowBadge,
                        contentColor = StockLowText
                    )
                }
                else -> {
                    Text(
                        text = "Stok: ${product.stock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!compact) {
                    Text(
                        text = DateTimeUtils.formatCurrencySimple(item.product.price),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 2.dp)
            ) {
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(if (compact) 24.dp else 30.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Kurang",
                        modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                    )
                }
                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.ExtraBold,
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = if (compact) 2.dp else 4.dp)
                )
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(if (compact) 24.dp else 30.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah",
                        modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                    )
                }
            }

            Text(
                text = DateTimeUtils.formatCurrencySimple(item.subtotal),
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = IncomeColor,
                modifier = Modifier.padding(horizontal = if (compact) 4.dp else 8.dp)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(if (compact) 24.dp else 30.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) (if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall) else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) (if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium) else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = if (isBold) IncomeColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
