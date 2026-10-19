package fr.ydel.gilets.util

object IdNormalizer {
    fun normalizeId(id: String): String {
        return id.replace("#", "")
            .replace(" ", "")
            .uppercase()
    }

    fun extractIdFromBarcode(barcode: String): String {
        val normalized = normalizeId(barcode)
        
        // Try to find a pattern that looks like an ID
        // Common patterns: digits only, digits with hyphens, etc.
        val patterns = listOf(
            Regex("\\d{2,}"), // 2 or more digits
            Regex("\\d{2,}-\\d{2,}"), // Pattern like 23-23
            Regex("[A-Z]\\d+"), // Pattern like A123
            Regex("\\d+[A-Z]+") // Pattern like 123A
        )

        for (pattern in patterns) {
            val match = pattern.find(normalized)
            if (match != null) {
                return normalizeId(match.value)
            }
        }

        return normalized
    }
}
