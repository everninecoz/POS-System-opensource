package com.posopensrc.printer

import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.domain.model.Transaction

object ReceiptBuilder {

    /**
     * Build receipt with adaptive paper size support
     * @param paperSize Paper size (58mm, 72mm, or 80mm)
     */
    fun buildReceipt(
        transaction: Transaction,
        storeName: String = "Warung Saya",
        storeAddress: String? = null,
        storePhone: String? = null,
        footer: String = "Terima kasih atas kunjungan Anda!",
        paperSize: PaperSize = PaperSize.WIDTH_80MM
    ): String {
        val sb = StringBuilder()
        val maxChars = paperSize.maxCharacters
        val halfWidth = maxChars / 2

        // Separator lines based on paper width
        val separator = "═".repeat(maxChars)
        val thinSeparator = "─".repeat(maxChars)

        // Header
        sb.appendLine(separator)
        sb.appendLine(centerText(storeName, maxChars))
        if (!storeAddress.isNullOrBlank()) {
            sb.appendLine(centerText(storeAddress, maxChars))
        }
        if (!storePhone.isNullOrBlank()) {
            sb.appendLine(centerText(storePhone, maxChars))
        }
        sb.appendLine(separator)

        // Transaction Info
        sb.appendLine("No: ${transaction.invoiceNumber}")
        sb.appendLine("Tanggal: ${DateTimeUtils.formatDateTime(transaction.createdAt)}")
        sb.appendLine("Kasir: Admin")
        sb.appendLine(thinSeparator)

        // Items - adaptive column width
        transaction.items.forEach { item ->
            val nameWidth = maxChars - 16 // Leave space for qty and price
            val name = item.productName.take(nameWidth).padEnd(nameWidth)
            val qty = item.quantity.toString().padStart(3)
            val price = DateTimeUtils.formatCurrencySimple(item.subtotal).padStart(12)
            sb.appendLine("$name $qty  $price")
        }

        // Summary
        sb.appendLine(thinSeparator)

        val labelWidth = maxChars - 14
        sb.appendLine("${"Subtotal:".padEnd(labelWidth)}${DateTimeUtils.formatCurrencySimple(transaction.subtotal).padStart(14)}")
        sb.appendLine("${"Pajak (${transaction.taxPercentage.toInt()}%):".padEnd(labelWidth)}${DateTimeUtils.formatCurrencySimple(transaction.taxAmount).padStart(14)}")

        sb.appendLine(separator)
        sb.appendLine("${"TOTAL:".padEnd(labelWidth)}${DateTimeUtils.formatCurrencySimple(transaction.total).padStart(14)}")
        sb.appendLine(separator)

        // Payment
        sb.appendLine("${"Bayar:".padEnd(labelWidth)}${DateTimeUtils.formatCurrencySimple(transaction.amountPaid).padStart(14)}")
        sb.appendLine("${"Kembalian:".padEnd(labelWidth)}${DateTimeUtils.formatCurrencySimple(transaction.changeAmount).padStart(14)}")
        sb.appendLine(separator)

        // Footer
        sb.appendLine()
        sb.appendLine(centerText(footer, maxChars))
        sb.appendLine()

        return sb.toString()
    }

    /**
     * Build simple receipt without transaction details (for testing)
     */
    fun buildTestReceipt(
        storeName: String = "POS UMKM",
        paperSize: PaperSize = PaperSize.WIDTH_80MM
    ): String {
        val sb = StringBuilder()
        val maxChars = paperSize.maxCharacters
        val separator = "═".repeat(maxChars)
        val thinSeparator = "─".repeat(maxChars)

        sb.appendLine(separator)
        sb.appendLine(centerText(storeName, maxChars))
        sb.appendLine(centerText("TEST PRINT", maxChars))
        sb.appendLine(separator)
        sb.appendLine()
        sb.appendLine(centerText("Printer berhasil", maxChars))
        sb.appendLine(centerText("terkoneksi!", maxChars))
        sb.appendLine()
        sb.appendLine(thinSeparator)
        sb.appendLine(centerText("Ukuran: ${paperSize.displayName}", maxChars))
        sb.appendLine(centerText("Karakter: ${maxChars}/baris", maxChars))
        sb.appendLine(thinSeparator)
        sb.appendLine()

        // Test line
        for (i in 1..5) {
            sb.appendLine("Baris test $i ${".".repeat(maxChars - 12)}")
        }

        sb.appendLine()
        sb.appendLine(separator)

        return sb.toString()
    }

    /**
     * Build receipt with DantSu library formatted tags
     */
    fun buildFormattedReceipt(
        transaction: Transaction,
        storeName: String = "Warung Saya",
        storeAddress: String? = null,
        storePhone: String? = null,
        footer: String = "Terima kasih atas kunjungan Anda!"
    ): String {
        val sb = StringBuilder()

        sb.appendLine("[C]<b><font size='big'>$storeName</font></b>")
        if (!storeAddress.isNullOrBlank()) {
            sb.appendLine("[C]$storeAddress")
        }
        if (!storePhone.isNullOrBlank()) {
            sb.appendLine("[C]$storePhone")
        }
        sb.appendLine("[C]================================")

        sb.appendLine("[L]No: ${transaction.invoiceNumber}")
        sb.appendLine("[L]Tanggal: ${DateTimeUtils.formatDateTime(transaction.createdAt)}")
        sb.appendLine("[L]Kasir: Admin")
        sb.appendLine("[C]--------------------------------")

        transaction.items.forEach { item ->
            sb.appendLine("[L]<b>${item.productName}</b>")
            sb.appendLine("[L]${item.quantity} x ${DateTimeUtils.formatCurrencySimple(item.price)}[R]${DateTimeUtils.formatCurrencySimple(item.subtotal)}")
        }

        sb.appendLine("[C]--------------------------------")
        sb.appendLine("[L]Subtotal[R]${DateTimeUtils.formatCurrencySimple(transaction.subtotal)}")
        sb.appendLine("[L]Pajak (${transaction.taxPercentage.toInt()}%)[R]${DateTimeUtils.formatCurrencySimple(transaction.taxAmount)}")
        sb.appendLine("[C]================================")
        sb.appendLine("[L]<b><font size='big'>TOTAL</font></b>[R]<b><font size='big'>${DateTimeUtils.formatCurrencySimple(transaction.total)}</font></b>")
        sb.appendLine("[C]================================")

        sb.appendLine("[L]Bayar[R]${DateTimeUtils.formatCurrencySimple(transaction.amountPaid)}")
        sb.appendLine("[L]Kembalian[R]${DateTimeUtils.formatCurrencySimple(transaction.changeAmount)}")
        sb.appendLine("[C]================================")

        sb.appendLine("[L]")
        sb.appendLine("[C]$footer")
        sb.appendLine("[L]")
        sb.appendLine("[L]")

        return sb.toString()
    }

    /**
     * Center text within given width
     */
    fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    /**
     * Create separator line
     */
    fun createSeparator(char: Char = '═', width: Int = 48): String {
        return char.toString().repeat(width)
    }
}
