package com.posopensrc.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.posopensrc.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: Product?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, costPrice: Double, stock: Int, minStock: Int, category: String?, barcode: String?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var costPriceText by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var stockText by remember { mutableStateOf(product?.stock?.toString() ?: "0") }
    var minStockText by remember { mutableStateOf(product?.minStock?.toString() ?: "5") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product != null) "Edit Produk" else "Tambah Produk"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) priceText = it },
                        label = { Text("Harga Jual") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) costPriceText = it },
                        label = { Text("Harga Beli") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stockText = it },
                        label = { Text("Stok") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minStockText = it },
                        label = { Text("Stok Minimal") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Kategori") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pilih Kategori") },
                            onClick = {
                                selectedCategory = ""
                                expanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toIntOrNull() ?: 0
                    val minStock = minStockText.toIntOrNull() ?: 5
                    val category = selectedCategory.ifBlank { null }
                    val barcodeValue = barcode.ifBlank { null }

                    if (name.isNotBlank() && price > 0) {
                        onSave(name, price, costPrice, stock, minStock, category, barcodeValue)
                    }
                },
                shape = RoundedCornerShape(12.dp)
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
