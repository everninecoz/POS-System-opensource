package com.posopensrc.ui.screens.products

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Product
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isSmallScreen = configuration.screenWidthDp < 600

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedProduct = null
                    showAddEditDialog = true
                },
                containerColor = GreenPrimary,
                modifier = Modifier.padding(if (isLandscape && isSmallScreen) 0.dp else 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(if (isLandscape && isSmallScreen) 8.dp else 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isLandscape && isSmallScreen) "Cari..." else "Cari produk...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(if (isLandscape && isSmallScreen) 10.dp else 12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isLandscape && isSmallScreen) 14.sp else 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape && isSmallScreen) 8.dp else 12.dp))

            // Category Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape && isSmallScreen) 4.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape && isSmallScreen) 4.dp else 8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                    label = { Text("Semua", fontSize = if (isLandscape && isSmallScreen) 12.sp else 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category, fontSize = if (isLandscape && isSmallScreen) 12.sp else 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape && isSmallScreen) 8.dp else 16.dp))

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isLandscape && isSmallScreen) 160.dp else 200.dp),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape && isSmallScreen) 8.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape && isSmallScreen) 8.dp else 12.dp)
            ) {
                items(uiState.filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        compact = isLandscape && isSmallScreen,
                        onEdit = {
                            selectedProduct = product
                            showAddEditDialog = true
                        },
                        onDelete = {
                            productToDelete = product
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    // ... rest of dialogs
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 10.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 36.dp else 48.dp)
                        .clip(RoundedCornerShape(if (compact) 8.dp else 12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (product.category?.firstOrNull() ?: product.name.firstOrNull() ?: 'P').toString().uppercase(),
                        fontSize = if (compact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(if (compact) 28.dp else 32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(if (compact) 28.dp else 32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

            Text(
                text = product.name,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = DateTimeUtils.formatCurrencySimple(product.price),
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IncomeColor
            )

            if (!compact) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stok: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.isLowStock) WarningColor
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (product.category != null) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
