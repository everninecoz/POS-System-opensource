package com.posopensrc.core.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.posopensrc.domain.model.Product
import com.posopensrc.domain.model.Transaction
import com.posopensrc.domain.model.TransactionItem
import com.posopensrc.domain.model.Customer
import com.posopensrc.domain.model.Discount
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportTransactions(
        context: Context,
        transactions: List<Transaction>,
        startDate: Long? = null,
        endDate: Long? = null
    ): Uri? {
        return try {
            val fileName = "transaksi_${dateFormat.format(Date())}.csv"
            val file = createFile(context, fileName)

            FileWriter(file).use { writer ->
                // Header
                writer.append("No,Invoice,Tanggal,Pelanggan,Subtotal,Pajak,Diskon,Total,Metode Bayar,Bayar,Kembalian,Status\n")

                // Data
                transactions.forEachIndexed { index, transaction ->
                    val date = dateOnlyFormat.format(Date(transaction.createdAt))
                    val customer = transaction.customerName ?: "-"
                    val status = if (transaction.isVoided) "Dibatalkan" else "Aktif"

                    writer.append("${index + 1},")
                    writer.append("${transaction.invoiceNumber},")
                    writer.append("$date,")
                    writer.append("$customer,")
                    writer.append("${transaction.subtotal},")
                    writer.append("${transaction.taxAmount},")
                    writer.append("${transaction.discount},")
                    writer.append("${transaction.total},")
                    writer.append("${transaction.paymentMethod},")
                    writer.append("${transaction.amountPaid},")
                    writer.append("${transaction.changeAmount},")
                    writer.append("$status\n")
                }
            }

            shareFile(context, file, "Laporan Transaksi")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportProducts(context: Context, products: List<Product>): Uri? {
        return try {
            val fileName = "produk_${dateFormat.format(Date())}.csv"
            val file = createFile(context, fileName)

            FileWriter(file).use { writer ->
                // Header
                writer.append("No,Nama,Barcode,Kategori,Harga Jual,Harga Beli,Stok,Stok Min,Satuan\n")

                // Data
                products.forEachIndexed { index, product ->
                    writer.append("${index + 1},")
                    writer.append("${escapeCsv(product.name)},")
                    writer.append("${product.barcode ?: "-"},")
                    writer.append("${escapeCsv(product.category ?: "-")},")
                    writer.append("${product.price},")
                    writer.append("${product.costPrice},")
                    writer.append("${product.stock},")
                    writer.append("${product.minStock},")
                    writer.append("-\n")
                }
            }

            shareFile(context, file, "Data Produk")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportCustomers(context: Context, customers: List<Customer>): Uri? {
        return try {
            val fileName = "pelanggan_${dateFormat.format(Date())}.csv"
            val file = createFile(context, fileName)

            FileWriter(file).use { writer ->
                // Header
                writer.append("No,Nama,Telepon,Email,Alamat,Total Belanja,Jumlah Transaksi,Poin\n")

                // Data
                customers.forEachIndexed { index, customer ->
                    writer.append("${index + 1},")
                    writer.append("${escapeCsv(customer.name)},")
                    writer.append("${customer.phone ?: "-"},")
                    writer.append("${customer.email ?: "-"},")
                    writer.append("${escapeCsv(customer.address ?: "-")},")
                    writer.append("${customer.totalPurchases},")
                    writer.append("${customer.purchaseCount},")
                    writer.append("${customer.loyaltyPoints}\n")
                }
            }

            shareFile(context, file, "Data Pelanggan")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportTransactionDetail(context: Context, transaction: Transaction): Uri? {
        return try {
            val fileName = "struk_${transaction.invoiceNumber}.csv"
            val file = createFile(context, fileName)

            FileWriter(file).use { writer ->
                writer.append("Detail Transaksi\n")
                writer.append("Invoice: ${transaction.invoiceNumber}\n")
                writer.append("Tanggal: ${dateOnlyFormat.format(Date(transaction.createdAt))}\n")
                writer.append("Pelanggan: ${transaction.customerName ?: "-"}\n")
                writer.append("\n")

                writer.append("No,Produk,Jumlah,Harga,Subtotal\n")

                transaction.items.forEachIndexed { index, item ->
                    writer.append("${index + 1},")
                    writer.append("${escapeCsv(item.productName)},")
                    writer.append("${item.quantity},")
                    writer.append("${item.price},")
                    writer.append("${item.subtotal}\n")
                }

                writer.append("\n")
                writer.append("Subtotal,,${transaction.subtotal}\n")
                writer.append("Pajak,,${transaction.taxAmount}\n")
                writer.append("Diskon,,${transaction.discount}\n")
                writer.append("TOTAL,,${transaction.total}\n")
                writer.append("Bayar,,${transaction.amountPaid}\n")
                writer.append("Kembalian,,${transaction.changeAmount}\n")
            }

            shareFile(context, file, "Detail Transaksi ${transaction.invoiceNumber}")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createFile(context: Context, fileName: String): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        return File(dir, fileName)
    }

    private fun shareFile(context: Context, file: File, title: String): Uri {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, title))
        return uri
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
