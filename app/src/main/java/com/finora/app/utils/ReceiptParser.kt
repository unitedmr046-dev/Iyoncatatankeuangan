package com.finora.app.utils

object ReceiptParser {
    fun parseAmount(text: String): Double? {
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .asReversed()

        val primaryKeywords = listOf("grand total", "total belanja", "total bayar", "total tagihan", "total")
        for (line in lines) {
            val lower = line.lowercase()
            if (lower.contains("subtotal")) continue
            if (primaryKeywords.any { lower.contains(it) }) {
                extractLastNumber(line)?.let { return it }
            }
        }

        val secondaryKeywords = listOf("jumlah", "amount", "subtotal")
        for (line in lines) {
            val lower = line.lowercase()
            if (secondaryKeywords.any { lower.contains(it) }) {
                extractLastNumber(line)?.let { return it }
            }
        }

        val candidates = Regex("""\d[\d.,]*""").findAll(text)
            .mapNotNull { match ->
                val raw = match.value
                // Abaikan nomor HP / kode panjang tanpa pemisah ribuan
                if (raw.startsWith("0") && raw.length > 4 && !raw.contains(".") && !raw.contains(",")) {
                    null
                } else {
                    normalizeNumber(raw)
                }
            }
            .filter { it in 100.0..100_000_000.0 }
            .toList()

        return candidates.maxOrNull()
    }

    private fun extractLastNumber(line: String): Double? {
        val matches = Regex("""\d[\d.,]*""").findAll(line).toList()
        return matches.asReversed().firstNotNullOfOrNull { normalizeNumber(it.value) }
    }

    private fun normalizeNumber(raw: String): Double? {
        val trimmed = raw.trim().trimEnd('.', ',')
        if (trimmed.isEmpty()) return null

        val hasDot = trimmed.contains('.')
        val hasComma = trimmed.contains(',')

        val normalized = when {
            hasDot && hasComma -> {
                if (trimmed.lastIndexOf(',') > trimmed.lastIndexOf('.')) {
                    trimmed.replace(".", "").replace(",", ".")
                } else {
                    trimmed.replace(",", "")
                }
            }
            hasDot -> {
                if (Regex("""^\d{1,3}(\.\d{3})+$""").matches(trimmed) || trimmed.count { it == '.' } > 1) {
                    trimmed.replace(".", "")
                } else {
                    trimmed
                }
            }
            hasComma -> {
                if (Regex("""^\d{1,3}(,\d{3})+$""").matches(trimmed) || trimmed.count { it == ',' } > 1) {
                    trimmed.replace(",", "")
                } else {
                    trimmed.replace(",", ".")
                }
            }
            else -> trimmed
        }

        return normalized.toDoubleOrNull()?.takeIf { it > 0.0 }
    }
}
