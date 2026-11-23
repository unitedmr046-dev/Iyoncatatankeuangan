package com.finora.app.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val indonesianLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()

    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        return formatter.format(amount)
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(millis))
    }

    fun currentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    fun monthKeyFromMillis(millis: Long): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date(millis))
    }

    fun currentMonthLabel(): String {
        return SimpleDateFormat("MMMM yyyy", indonesianLocale).format(Date())
    }
}
