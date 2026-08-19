package com.posopensrc.core.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id"))
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id"))
    private val invoiceDateFormat = SimpleDateFormat("yyyyMMdd", Locale("id"))
    private val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id"))

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun formatInvoiceDate(timestamp: Long): String = invoiceDateFormat.format(Date(timestamp))

    fun formatFullDate(timestamp: Long): String = fullDateFormat.format(Date(timestamp))

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    fun formatCurrencySimple(amount: Double): String {
        return "Rp ${amount.toLong().toString().reversed().chunked(3).joinToString(".").reversed()}"
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Baru saja"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} menit lalu"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} jam lalu"
            diff < TimeUnit.DAYS.toMillis(2) -> "Kemarin"
            diff < TimeUnit.DAYS.toMillis(7) -> "${diff / TimeUnit.DAYS.toMillis(1)} hari lalu"
            else -> formatDate(timestamp)
        }
    }

    fun isToday(timestamp: Long): Boolean {
        val today = invoiceDateFormat.format(Date())
        val target = invoiceDateFormat.format(Date(timestamp))
        return today == target
    }

    fun isThisWeek(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff < TimeUnit.DAYS.toMillis(7)
    }

    fun isThisMonth(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val calNow = java.util.Calendar.getInstance()
        val calTarget = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

        return calNow.get(java.util.Calendar.YEAR) == calTarget.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.MONTH) == calTarget.get(java.util.Calendar.MONTH)
    }

    fun generateInvoiceNumber(sequenceNumber: Int): String {
        val date = invoiceDateFormat.format(Date())
        return "${AppConstants.INVOICE_PREFIX}-$date-${sequenceNumber.toString().padStart(3, '0')}"
    }
}
