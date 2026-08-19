package com.posopensrc.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.posopensrc.printer.PaperSize
import com.posopensrc.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onToggleDarkMode: () -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()

    var showEditStoreDialog by remember { mutableStateOf(false) }
    var showEditReceiptDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Store Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Informasi Toko",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showEditStoreDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsItem(
                    title = "Nama Toko",
                    value = uiState.settings.storeName
                )

                SettingsItem(
                    title = "Alamat",
                    value = uiState.settings.storeAddress ?: "-"
                )

                SettingsItem(
                    title = "No. Telepon",
                    value = uiState.settings.storePhone ?: "-"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Receipt Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan Struk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showEditReceiptDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsItem(
                    title = "Footer Struk",
                    value = uiState.settings.receiptFooter
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Appearance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tampilan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode Gelap",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GreenPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bahasa",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (language == "id") "Indonesia" else "English",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Language toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = language == "id",
                        onClick = { onChangeLanguage("id") },
                        label = { Text("Indonesia") }
                    )
                    FilterChip(
                        selected = language == "en",
                        onClick = { onChangeLanguage("en") },
                        label = { Text("English") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tax Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pajak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsItem(
                    title = "Pajak",
                    value = "${uiState.settings.taxPercentage.toInt()}%"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Printer Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pengaturan Printer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Paper Size Selection
                Text(
                    text = "Ukuran Kertas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaperSize.entries.forEach { size ->
                        FilterChip(
                            selected = uiState.selectedPaperSize == size,
                            onClick = { viewModel.onPaperSizeSelected(size) },
                            label = { Text(size.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (uiState.selectedPaperSize) {
                        PaperSize.WIDTH_58MM -> "Mini printer (32 karakter/baris)"
                        PaperSize.WIDTH_72MM -> "Printer sedang (42 karakter/baris)"
                        PaperSize.WIDTH_80MM -> "Printer standar (48 karakter/baris)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info Card - Printer Compatibility
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    text = "Info Printer Universal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aplikasi mendukung berbagai jenis printer:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                val printOptions = listOf(
                    "1. Printer Thermal (Bluetooth/USB) - 58mm/72mm/80mm",
                    "2. Printer Inkjet/Laser - Via Android Print",
                    "3. Printer WiFi - Via Android Print",
                    "4. Simpan sebagai PDF - Untuk cetak nanti",
                    "5. Simpan sebagai Gambar - Untuk share",
                    "6. Share via WhatsApp/Email - Kirim struk digital"
                )

                printOptions.forEach { option ->
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }

    // Edit Store Info Dialog
    if (showEditStoreDialog) {
        EditStoreInfoDialog(
            storeName = uiState.settings.storeName,
            storeAddress = uiState.settings.storeAddress ?: "",
            storePhone = uiState.settings.storePhone ?: "",
            onDismiss = { showEditStoreDialog = false },
            onConfirm = { name, address, phone ->
                viewModel.updateStoreInfo(name, address.ifBlank { null }, phone.ifBlank { null })
                showEditStoreDialog = false
            }
        )
    }

    // Edit Receipt Dialog
    if (showEditReceiptDialog) {
        EditReceiptDialog(
            footer = uiState.settings.receiptFooter,
            onDismiss = { showEditReceiptDialog = false },
            onConfirm = { footer ->
                viewModel.updateReceiptFooter(footer)
                showEditReceiptDialog = false
            }
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditStoreInfoDialog(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(storeName) }
    var address by remember { mutableStateOf(storeAddress) }
    var phone by remember { mutableStateOf(storePhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Informasi Toko") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Toko *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. Telepon") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, address, phone)
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
private fun EditReceiptDialog(
    footer: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var footerText by remember { mutableStateOf(footer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Footer Struk") },
        text = {
            Column {
                Text(
                    text = "Teks ini akan ditampilkan di bagian bawah struk:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it },
                    label = { Text("Footer Struk") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(footerText) }) {
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
