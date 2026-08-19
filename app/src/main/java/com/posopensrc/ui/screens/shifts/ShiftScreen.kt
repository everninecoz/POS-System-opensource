package com.posopensrc.ui.screens.shifts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.posopensrc.domain.model.Shift
import com.posopensrc.ui.theme.GreenPrimary
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    viewModel: ShiftViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
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
                    text = "Manajemen Shift",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = viewModel::toggleShiftHistory) {
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
            } else if (uiState.currentShift != null) {
                // Current Shift Status
                CurrentShiftCard(
                    shift = uiState.currentShift!!,
                    onCloseShift = viewModel::showCloseShiftDialog
                )
            } else {
                // No Active Shift
                NoShiftCard(
                    onOpenShift = viewModel::showOpenShiftDialog
                )
            }

            // Shift History
            if (uiState.showShiftHistory) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Riwayat Shift",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.recentShifts.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat shift",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.recentShifts) { shift ->
                            ShiftHistoryItem(shift = shift)
                        }
                    }
                }
            }
        }
    }

    // Open Shift Dialog
    if (uiState.showOpenShiftDialog) {
        OpenShiftDialog(
            onDismiss = viewModel::hideOpenShiftDialog,
            onConfirm = viewModel::openShift
        )
    }

    // Close Shift Dialog
    if (uiState.showCloseShiftDialog) {
        CloseShiftDialog(
            shift = uiState.currentShift!!,
            onDismiss = viewModel::hideCloseShiftDialog,
            onConfirm = viewModel::closeShift
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
private fun CurrentShiftCard(
    shift: Shift,
    onCloseShift: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shift Aktif",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Saldo Awal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = DateTimeUtils.formatCurrencySimple(shift.openingBalance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "Total Penjualan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = DateTimeUtils.formatCurrencySimple(shift.totalSales),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeColor
                    )
                }
                Column {
                    Text(
                        text = "Transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${shift.totalTransactions}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dibuka",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = DateTimeUtils.formatDateTime(shift.openedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "Durasi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatDuration(shift.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCloseShift,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
                Text("Tutup Shift")
            }
        }
    }
}

@Composable
private fun NoShiftCard(
    onOpenShift: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tidak Ada Shift Aktif",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Buka shift baru untuk memulai transaksi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenShift,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text("Buka Shift Baru")
            }
        }
    }
}

@Composable
private fun ShiftHistoryItem(shift: Shift) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateTimeUtils.formatDate(shift.openedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${DateTimeUtils.formatTime(shift.openedAt)} - ${
                        shift.closedAt?.let { DateTimeUtils.formatTime(it) } ?: "Aktif"
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeUtils.formatCurrencySimple(shift.totalSales),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = IncomeColor
                )
                if (shift.difference != 0.0) {
                    Text(
                        text = "Selisih: ${DateTimeUtils.formatCurrencySimple(shift.difference)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (shift.difference > 0) IncomeColor else WarningColor
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenShiftDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var openingBalance by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buka Shift Baru") },
        text = {
            Column {
                Text(
                    text = "Masukkan saldo awal kas:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("Saldo Awal (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balance = openingBalance.toDoubleOrNull() ?: 0.0
                    if (balance >= 0) {
                        onConfirm(balance)
                    }
                }
            ) {
                Text("Buka Shift")
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
private fun CloseShiftDialog(
    shift: Shift,
    onDismiss: () -> Unit,
    onConfirm: (closingBalance: Double, actualBalance: Double, notes: String?) -> Unit
) {
    var closingBalance by remember { mutableStateOf("") }
    var actualBalance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tutup Shift") },
        text = {
            Column {
                Text(
                    text = "Total Penjualan: ${DateTimeUtils.formatCurrencySimple(shift.totalSales)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Saldo Diharapkan: ${DateTimeUtils.formatCurrencySimple(shift.expectedBalance)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = closingBalance,
                    onValueChange = { closingBalance = it },
                    label = { Text("Saldo Penutupan (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = actualBalance,
                    onValueChange = { actualBalance = it },
                    label = { Text("Saldo Aktual (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Show difference
                val actual = actualBalance.toDoubleOrNull() ?: 0.0
                val expected = shift.expectedBalance
                val difference = actual - expected

                if (actual > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selisih: ${DateTimeUtils.formatCurrencySimple(difference)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            difference > 0 -> IncomeColor
                            difference < 0 -> WarningColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val closing = closingBalance.toDoubleOrNull() ?: 0.0
                    val actual = actualBalance.toDoubleOrNull() ?: 0.0
                    if (closing >= 0 && actual >= 0) {
                        onConfirm(closing, actual, notes.ifBlank { null })
                    }
                }
            ) {
                Text("Tutup Shift")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val hours = durationMs / (1000 * 60 * 60)
    val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
    return "${hours}j ${minutes}m"
}
