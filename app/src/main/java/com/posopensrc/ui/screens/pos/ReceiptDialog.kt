package com.posopensrc.ui.screens.pos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Transaction
import com.posopensrc.printer.UniversalPrintManager
import com.posopensrc.ui.components.bounceClick
import com.posopensrc.ui.theme.IncomeColor
import com.posopensrc.ui.theme.ReceiptPaperBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val printManager = remember { UniversalPrintManager.getInstance(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IncomeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaksi Berhasil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Realistic Thermal Paper Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ReceiptPaperBg
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = buildReceiptText(transaction),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }

                // Action Print Options Section
                Text(
                    text = "Aksi & Cetak Struk",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                // Row 1: Direct Print Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrintOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bluetooth,
                        label = "Thermal BT",
                        enabled = printManager.isBluetoothConnected(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val success = printManager.printViaBluetooth(transaction)
                            Toast.makeText(
                                context,
                                if (success) "Berhasil cetak!" else "Gagal cetak. Periksa koneksi bluetooth.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    PrintOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Print,
                        label = "Semua Printer",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            printManager.printViaAndroidPrint(transaction)
                        }
                    )
                }

                // Row 2: Save & Export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrintOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PictureAsPdf,
                        label = "Simpan PDF",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val uri = printManager.saveAsPdf(transaction)
                            Toast.makeText(
                                context,
                                if (uri != null) "PDF berhasil disimpan!" else "Gagal menyimpan PDF",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    PrintOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Share,
                        label = "Bagikan Struk",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            printManager.shareAsPdf(transaction)
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Selesai / Transaksi Baru",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun PrintOptionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .bounceClick(),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun buildReceiptText(transaction: Transaction): String {
    val sb = StringBuilder()
    val maxChars = 40
    val separator = "═".repeat(maxChars)
    val thinSeparator = "─".repeat(maxChars)

    sb.appendLine(separator)
    sb.appendLine(centerText("POS KASIR PRO", maxChars))
    sb.appendLine(centerText("Jl. Bisnis Retail No. 88", maxChars))
    sb.appendLine(separator)
    sb.appendLine("No     : ${transaction.invoiceNumber}")
    sb.appendLine("Waktu  : ${DateTimeUtils.formatDateTime(transaction.createdAt)}")
    sb.appendLine("Metode : ${transaction.paymentMethod.uppercase()}")
    sb.appendLine(thinSeparator)

    transaction.items.forEach { item ->
        val name = item.productName.take(20).padEnd(20)
        val qty = "x${item.quantity}".padStart(4)
        val price = DateTimeUtils.formatCurrencySimple(item.subtotal).padStart(14)
        sb.appendLine("$name$qty$price")
    }

    sb.appendLine(thinSeparator)
    sb.appendLine("${"Subtotal:".padEnd(26)}${DateTimeUtils.formatCurrencySimple(transaction.subtotal).padStart(14)}")
    if (transaction.taxAmount > 0) {
        sb.appendLine("${"Pajak (${transaction.taxPercentage.toInt()}%):".padEnd(26)}${DateTimeUtils.formatCurrencySimple(transaction.taxAmount).padStart(14)}")
    }
    sb.appendLine(separator)
    sb.appendLine("${"TOTAL:".padEnd(24)}${DateTimeUtils.formatCurrencySimple(transaction.total).padStart(16)}")
    sb.appendLine(separator)
    sb.appendLine("${"Bayar:".padEnd(26)}${DateTimeUtils.formatCurrencySimple(transaction.amountPaid).padStart(14)}")
    sb.appendLine("${"Kembalian:".padEnd(26)}${DateTimeUtils.formatCurrencySimple(transaction.changeAmount).padStart(14)}")
    sb.appendLine(separator)
    sb.appendLine()
    sb.appendLine(centerText("Terima kasih atas kunjungan Anda!", maxChars))

    return sb.toString()
}

private fun centerText(text: String, width: Int): String {
    if (text.length >= width) return text.take(width)
    val padding = (width - text.length) / 2
    return " ".repeat(padding) + text
}
