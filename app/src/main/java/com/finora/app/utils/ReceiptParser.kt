package com.finora.app.utils

object ReceiptParser {
    fun parseAmount(text: String): Double? {
        // Cari kata kunci total/jumlah/rp diikuti angka
        val regex = Regex("""(?i)(?:total|jumlah|amount|rp|idr)\s*[:.]?\s*([\d.,]+)""")
        val match = regex.find(text)
        
        if (match != null) {
            val rawAmount = match.groupValues[1]
            // Bersihkan titik (ribuan) dan ubah koma jadi titik (desimal)
            val cleanString = rawAmount.replace(".", "").replace(",", ".")
            return cleanString.toDoubleOrNull()
        }
        
        // Fallback: Kalau nggak nemu kata kunci, cari angka terbesar di struk
        val numbers = Regex("""[\d.,]+""").findAll(text).mapNotNull {
            it.value.replace(".", "").replace(",", ".").toDoubleOrNull()
        }
        return numbers.maxOrNull()
    }
}
