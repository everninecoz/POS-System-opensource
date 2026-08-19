package com.posopensrc.ui.screens.discounts

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Discount
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountListScreen(
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddEditDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Diskon")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Diskon & Promo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari diskon...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredDiscounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "Belum ada diskon",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredDiscounts) { discount ->
                        DiscountItem(
                            discount = discount,
                            onClick = { viewModel.onDiscountSelected(discount) },
                            onEdit = { viewModel.showAddEditDialog(discount) },
                            onDelete = { viewModel.deleteDiscount(discount.id) },
                            onToggleActive = { viewModel.toggleDiscountStatus(discount) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (uiState.showAddEditDialog) {
        AddEditDiscountDialog(
            discount = uiState.editingDiscount,
            onDismiss = viewModel::hideAddEditDialog,
            onConfirm = { discount ->
                if (uiState.editingDiscount != null) {
                    viewModel.updateDiscount(discount)
                } else {
                    viewModel.createDiscount(
                        name = discount.name,
                        code = discount.code,
                        description = discount.description,
                        discountType = discount.discountType,
                        discountValue = discount.discountValue,
                        minPurchase = discount.minPurchase,
                        maxDiscount = discount.maxDiscount,
                        buyQuantity = discount.buyQuantity,
                        getQuantity = discount.getQuantity,
                        usageLimit = discount.usageLimit,
                        validFrom = discount.validFrom,
                        validUntil = discount.validUntil
                    )
                }
            }
        )
    }

    // Discount Detail Dialog
    uiState.selectedDiscount?.let { discount ->
        DiscountDetailDialog(
            discount = discount,
            onDismiss = { viewModel.onDiscountSelected(discount.copy()) },
            onEdit = {
                viewModel.showAddEditDialog(discount)
                viewModel.onDiscountSelected(discount.copy())
            }
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
private fun DiscountItem(
    discount: Discount,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (discount.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = if (discount.isActive) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = discount.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    discount.code?.let { code ->
                        Text(
                            text = "Kode: $code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = when (discount.discountType) {
                            "percentage" -> "Diskon ${discount.discountValue.toInt()}%"
                            "fixed" -> "Diskon ${DateTimeUtils.formatCurrencySimple(discount.discountValue)}"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (discount.minPurchase > 0) {
                        Text(
                            text = "Min. belanja: ${DateTimeUtils.formatCurrencySimple(discount.minPurchase)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = discount.isActive,
                    onCheckedChange = { onToggleActive() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEditDiscountDialog(
    discount: Discount?,
    onDismiss: () -> Unit,
    onConfirm: (Discount) -> Unit
) {
    val name = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.name ?: "") }
    val code = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.code ?: "") }
    val description = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.description ?: "") }
    val discountType = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.discountType ?: "percentage") }
    val discountValue = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.discountValue?.toString() ?: "") }
    val minPurchase = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.minPurchase?.toString() ?: "") }
    val maxDiscount = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.maxDiscount?.toString() ?: "") }
    val usageLimit = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(discount?.usageLimit?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (discount != null) "Edit Diskon" else "Tambah Diskon"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Nama Diskon *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code.value,
                    onValueChange = { code.value = it },
                    label = { Text("Kode Diskon") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tipe Diskon:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = discountType.value == "percentage",
                        onClick = { discountType.value = "percentage" },
                        label = { Text("Persen (%)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = discountType.value == "fixed",
                        onClick = { discountType.value = "fixed" },
                        label = { Text("Nominal (Rp)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = discountValue.value,
                    onValueChange = { discountValue.value = it },
                    label = {
                        Text(
                            if (discountType.value == "percentage") "Nilai Diskon (%)"
                            else "Nilai Diskon (Rp)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minPurchase.value,
                    onValueChange = { minPurchase.value = it },
                    label = { Text("Minimal Pembelian (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxDiscount.value,
                    onValueChange = { maxDiscount.value = it },
                    label = { Text("Maksimal Diskon (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = usageLimit.value,
                    onValueChange = { usageLimit.value = it },
                    label = { Text("Batas Penggunaan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.value.isNotBlank() && discountValue.value.isNotBlank()) {
                        onConfirm(
                            Discount(
                                id = discount?.id ?: 0,
                                name = name.value,
                                code = code.value.ifBlank { null },
                                description = description.value.ifBlank { null },
                                discountType = discountType.value,
                                discountValue = discountValue.value.toDoubleOrNull() ?: 0.0,
                                minPurchase = minPurchase.value.toDoubleOrNull() ?: 0.0,
                                maxDiscount = maxDiscount.value.toDoubleOrNull(),
                                usageLimit = usageLimit.value.toIntOrNull(),
                                isActive = discount?.isActive ?: true,
                                validFrom = discount?.validFrom ?: System.currentTimeMillis(),
                                validUntil = discount?.validUntil ?: Long.MAX_VALUE
                            )
                        )
                    }
                }
            ) {
                Text("Simpan")
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
private fun DiscountDetailDialog(
    discount: Discount,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Diskon") },
        text = {
            Column {
                Text(
                    text = discount.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                discount.code?.let { code ->
                    DetailRow(label = "Kode", value = code)
                }
                discount.description?.let { desc ->
                    DetailRow(label = "Deskripsi", value = desc)
                }

                DetailRow(
                    label = "Tipe",
                    value = if (discount.discountType == "percentage") "Persen (%)" else "Nominal (Rp)"
                )
                DetailRow(
                    label = "Nilai",
                    value = if (discount.discountType == "percentage") "${discount.discountValue.toInt()}%"
                    else DateTimeUtils.formatCurrencySimple(discount.discountValue)
                )

                if (discount.minPurchase > 0) {
                    DetailRow(label = "Min. Pembelian", value = DateTimeUtils.formatCurrencySimple(discount.minPurchase))
                }
                discount.maxDiscount?.let { max ->
                    DetailRow(label = "Maks. Diskon", value = DateTimeUtils.formatCurrencySimple(max))
                }
                discount.usageLimit?.let { limit ->
                    DetailRow(label = "Batas Penggunaan", value = "$limit kali (digunakan: ${discount.usageCount} kali)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Status: ${if (discount.isActive) "Aktif" else "Nonaktif"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (discount.isActive) IncomeColor else WarningColor
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
