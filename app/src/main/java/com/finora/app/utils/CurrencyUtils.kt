package com.finora.app.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyUtils {
    private val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val formatter = DecimalFormat("#,##0", symbols)

    fun formatRupiah(amount: Double): String {
        val prefix = if (amount < 0) "-Rp " else "Rp "
        return prefix + formatter.format(abs(amount))
    }

    fun formatCompact(amount: Double): String {
        val absVal = abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            absVal >= 1_000_000_000 -> "${sign}${String.format(Locale.US, "%.1f", absVal / 1_000_000_000)}M"
            absVal >= 1_000_000 -> "${sign}${String.format(Locale.US, "%.1f", absVal / 1_000_000)}jt"
            absVal >= 1_000 -> "${sign}${String.format(Locale.US, "%.0f", absVal / 1_000)}rb"
            else -> "${sign}${absVal.toLong()}"
        }
    }
}
