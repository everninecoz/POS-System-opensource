package com.posopensrc.ui.screens.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Product
import com.posopensrc.domain.model.StockOpname
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOpnameScreen(
    viewModel: StockOpnameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Stock Opname")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock Opname",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = viewModel::toggleHistory) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null
                    )
                    Text("Riwayat")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.showHistory) {
                // History View
                if (uiState.stockOpnames.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat stock opname",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.stockOpnames) { opname ->
                            StockOpnameHistoryItem(opname = opname)
                        }
                    }
                }
            } else {
                // Current Stock View
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Produk: ${uiState.products.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val lowStockCount = uiState.products.count { it.stock <= it.minStock }
                        if (lowStockCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$lowStockCount produk stok menipis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarningColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products) { product ->
                        StockItem(
                            product = product,
                            physicalStock = uiState.selectedProducts[product.id] ?: product.stock,
                            onStockChanged = { viewModel.onPhysicalStockChanged(product.id, it) }
                        )
                    }
                }
            }
        }
    }

    // Create Dialog
    if (uiState.showCreateDialog) {
        CreateStockOpnameDialog(
            onDismiss = viewModel::hideCreateDialog,
            onConfirm = viewModel::createStockOpname
        )
    }

    // Last Opname Result Dialog
    uiState.lastOpname?.let { opname ->
        StockOpnameResultDialog(
            opname = opname,
            onDismiss = { viewModel.clearError() }
        )
    }

    // Error Dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StockItem(
    product: Product,
    physicalStock: Int,
    onStockChanged: (Int) -> Unit
) {
    val difference = physicalStock - product.stock

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
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Stok Sistem: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (physicalStock > 0) onStockChanged(physicalStock - 1) }
                ) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }

                Text(
                    text = "$physicalStock",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = { onStockChanged(physicalStock + 1) }
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (difference != 0) {
                    Text(
                        text = if (difference > 0) "+$difference" else "$difference",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (difference > 0) IncomeColor else WarningColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StockOpnameHistoryItem(opname: StockOpname) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateTimeUtils.formatDate(opname.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${opname.totalItems} produk diperiksa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (opname.itemsWithDifference > 0) {
                    Text(
                        text = "${opname.itemsWithDifference} selisih",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningColor,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Tidak ada selisih",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IncomeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateStockOpnameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Stock Opname") },
        text = {
            Column {
                Text(
                    text = "Stok fisik akan disesuaikan dengan stok sistem.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(notes.ifBlank { null }) }
            ) {
                Text("Buat Stock Opname")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun StockOpnameResultDialog(
    opname: StockOpname,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Opname Selesai") },
        text = {
            Column {
                Text(
                    text = "Total Produk: ${opname.totalItems}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Selisih: ${opname.itemsWithDifference} produk",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (opname.itemsWithDifference > 0) WarningColor else IncomeColor
                )

                if (opname.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Detail Selisih:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    opname.items.filter { it.difference != 0 }.forEach { item ->
                        Text(
                            text = "${item.productName}: ${item.systemStock} → ${item.physicalStock} (${if (item.difference > 0) "+" else ""}${item.difference})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.difference > 0) IncomeColor else WarningColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
