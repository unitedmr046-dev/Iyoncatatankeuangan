package com.finora.app.utils

object ReceiptParser {

    private val priorityKeywords = listOf(
        "grand total",
        "total bayar",
        "total belanja",
        "total tagihan",
        "total harga",
        "jumlah bayar",
        "amount due",
        "total pembayaran",
        "total",
        "netto",
        "tunai"
    )

    private val ignoredLineKeywords = listOf(
        "subtotal",
        "sub total",
        "sub-total",
        "qty",
        "item",
        "kembali",
        "kembalian",
        "change",
        "diskon",
        "discount",
        "hemat",
        "ppn",
        "pajak",
        "tax",
        "npwp",
        "telp",
        "hp",
        "wa",
        "no.",
        "nota",
        "struk",
        "kasir"
    )

    private val numberRegex = Regex("""(?i)(?:rp\.?|idr)?\s*(\d+(?:[.,]\d{3})*(?:[.,]\d{1,2})?)""")

    fun parseAmount(text: String): Double? {
        if (text.isBlank()) return null
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 1. Cari baris yang mengandung kata kunci total (prioritas tertinggi dulu)
        for (keyword in priorityKeywords) {
            val matchingLines = lines.filter { line ->
                val lower = line.lowercase()
                lower.contains(keyword) && ignoredLineKeywords.none { ignored ->
                    ignored != keyword && lower.contains(ignored)
                }
            }
            for (line in matchingLines.asReversed()) {
                val amount = extractLastReasonableNumber(line)
                if (amount != null && amount >= 100.0) {
                    return amount
                }
            }
        }

        // 2. Fallback: cari angka berformat uang yang paling masuk akal
        val candidates = lines
            .filter { line ->
                val lower = line.lowercase()
                ignoredLineKeywords.none { lower.contains(it) }
            }
            .flatMap { line ->
                numberRegex.findAll(line).mapNotNull { match ->
                    parseLocalizedNumber(match.groupValues[1])
                }.toList()
            }
            .filter { it in 500.0..500_000_000.0 }

        return candidates.maxOrNull()
    }

    private fun extractLastReasonableNumber(line: String): Double? {
        val matches = numberRegex.findAll(line).toList()
        return matches.asReversed()
            .mapNotNull { parseLocalizedNumber(it.groupValues[1]) }
            .firstOrNull { it in 100.0..500_000_000.0 }
    }

    internal fun parseLocalizedNumber(raw: String): Double? {
        val cleaned = raw.trim()
        if (cleaned.isEmpty()) return null

        // Tolak deretan digit terlalu panjang tanpa pemisah (barcode / nomor HP)
        if (!cleaned.contains('.') && !cleaned.contains(',') && cleaned.length >= 10) {
            return null
        }

        val lastDot = cleaned.lastIndexOf('.')
        val lastComma = cleaned.lastIndexOf(',')

        val normalized = when {
            lastDot >= 0 && lastComma >= 0 -> {
                if (lastComma > lastDot) {
                    cleaned.replace(".", "").replace(",", ".")
                } else {
                    cleaned.replace(",", "")
                }
            }
            lastDot >= 0 -> {
                val afterDot = cleaned.substring(lastDot + 1)
                if (afterDot.length == 3) {
                    cleaned.replace(".", "")
                } else if (afterDot.length <= 2 && cleaned.count { it == '.' } == 1) {
                    cleaned
                } else {
                    cleaned.replace(".", "")
                }
            }
            lastComma >= 0 -> {
                val afterComma = cleaned.substring(lastComma + 1)
                if (afterComma.length == 3) {
                    cleaned.replace(",", "")
                } else if (afterComma.length <= 2 && cleaned.count { it == ',' } == 1) {
                    cleaned.replace(",", ".")
                } else {
                    cleaned.replace(",", "")
                }
            }
            else -> cleaned
        }

        return normalized.toDoubleOrNull()
    }
}
