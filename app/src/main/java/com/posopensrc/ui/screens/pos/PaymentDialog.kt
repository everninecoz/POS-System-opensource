package com.posopensrc.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.PaymentMethod
import com.posopensrc.ui.components.bounceClick
import com.posopensrc.ui.theme.IncomeColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentDialog(
    total: Double,
    onDismiss: () -> Unit,
    onPayment: (PaymentMethod, Double) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amountPaidText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
    val change = if (selectedMethod == PaymentMethod.CASH) amountPaid - total else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pembayaran Kasir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Tagihan Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL TAGIHAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateTimeUtils.formatCurrencySimple(total),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Payment Method Selector
                Text(
                    text = "Metode Pembayaran",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodButton(
                        modifier = Modifier.weight(1f),
                        method = PaymentMethod.CASH,
                        icon = Icons.Default.Payments,
                        label = "Tunai",
                        isSelected = selectedMethod == PaymentMethod.CASH,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedMethod = PaymentMethod.CASH
                        }
                    )
                    PaymentMethodButton(
                        modifier = Modifier.weight(1f),
                        method = PaymentMethod.QRIS,
                        icon = Icons.Default.QrCodeScanner,
                        label = "QRIS",
                        isSelected = selectedMethod == PaymentMethod.QRIS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedMethod = PaymentMethod.QRIS
                        }
                    )
                    PaymentMethodButton(
                        modifier = Modifier.weight(1f),
                        method = PaymentMethod.TRANSFER,
                        icon = Icons.Default.AccountBalance,
                        label = "Transfer",
                        isSelected = selectedMethod == PaymentMethod.TRANSFER,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedMethod = PaymentMethod.TRANSFER
                        }
                    )
                }

                // Cash Amount Input & Quick Cash Buttons
                if (selectedMethod == PaymentMethod.CASH) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = amountPaidText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' }) {
                                    amountPaidText = newValue
                                }
                            },
                            label = { Text("Jumlah Uang Diterima") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            prefix = { Text("Rp ", fontWeight = FontWeight.Bold) }
                        )

                        // Quick Cash Suggestions
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickCashChip(
                                label = "Uang Pas",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    amountPaidText = total.toInt().toString()
                                }
                            )
                            val suggestions = listOf(50000.0, 100000.0)
                            suggestions.forEach { sug ->
                                if (sug >= total) {
                                    QuickCashChip(
                                        label = DateTimeUtils.formatCurrencySimple(sug),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            amountPaidText = sug.toInt().toString()
                                        }
                                    )
                                }
                            }
                        }

                        // Change Calculation Box
                        if (amountPaid > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (change >= 0) MaterialTheme.colorScheme.surfaceContainerHigh
                                    else MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (change >= 0) "Kembalian" else "Kurang",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (change >= 0) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = DateTimeUtils.formatCurrencySimple(if (change >= 0) change else -change),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (change >= 0) IncomeColor
                                        else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPayment(selectedMethod, if (selectedMethod == PaymentMethod.CASH) amountPaid else total)
                },
                modifier = Modifier.bounceClick(),
                shape = RoundedCornerShape(12.dp),
                enabled = if (selectedMethod == PaymentMethod.CASH) amountPaid >= total else true
            ) {
                Text(
                    text = "Konfirmasi & Cetak",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.bounceClick()
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun QuickCashChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .bounceClick(scaleDown = 0.92f, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PaymentMethodButton(
    modifier: Modifier = Modifier,
    method: PaymentMethod,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .bounceClick(scaleDown = 0.95f, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        ),
        border = if (!isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
