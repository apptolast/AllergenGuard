package org.apptolast.menuadmin.domain.model

enum class AllergenType(
    val id: Int,
    val jsonKey: String,
    val apiCode: String,
    val nameEs: String,
    val nameEn: String,
    val iconCode: Int,
    // ARGB color as a plain Long so the domain stays free of Compose. Map to Compose Color in the
    // UI layer via the `AllergenType.color` extension.
    val colorArgb: Long,
) {
    GLUTEN(1, "gluten", "GLUTEN", "Gluten", "Gluten", 0xE39E, 0xFFF59E0B),
    CRUSTACEANS(2, "crustaceans", "CRUSTACEANS", "Crustáceos", "Crustaceans", 0xE4F7, 0xFFEF4444),
    EGGS(3, "eggs", "EGGS", "Huevos", "Eggs", 0xE25D, 0xFFF97316),
    FISH(4, "fish", "FISH", "Pescado", "Fish", 0xE3A6, 0xFF3B82F6),
    PEANUTS(5, "peanuts", "PEANUTS", "Cacahuetes", "Peanuts", 0xE39B, 0xFF92400E),
    SOY(6, "soy", "SOYA", "Soja", "Soy", 0xE38F, 0xFF84CC16),
    DAIRY(7, "milk", "MILK", "Lácteos", "Dairy", 0xE399, 0xFF60A5FA),
    TREE_NUTS(8, "nuts", "TREE_NUTS", "Frutos Secos", "Tree Nuts", 0xE2F5, 0xFF78350F),
    CELERY(9, "celery", "CELERY", "Apio", "Celery", 0xE2DE, 0xFF22C55E),
    MUSTARD(10, "mustard", "MUSTARD", "Mostaza", "Mustard", 0xE0D6, 0xFFEAB308),
    SESAME(11, "sesame", "SESAME", "Sésamo", "Sesame", 0xE345, 0xFF9CA3AF),
    SULFITES(12, "sulphites", "SULPHITES", "Sulfitos", "Sulfites", 0xE0D5, 0xFF8B5CF6),
    LUPINS(13, "lupins", "LUPIN", "Altramuces", "Lupins", 0xE2D4, 0xFFEC4899),
    MOLLUSKS(14, "molluscs", "MOLLUSCS", "Moluscos", "Mollusks", 0xE4F8, 0xFF14B8A6),
    ;

    /** Material icon glyph (private-use code point) for the UI. */
    val icon: Char get() = iconCode.toChar()

    companion object {
        private val byJsonKey: Map<String, AllergenType> = entries.associateBy { it.jsonKey }
        private val byApiCode: Map<String, AllergenType> = entries.associateBy { it.apiCode }

        fun fromJsonKey(key: String): AllergenType? = byJsonKey[key.lowercase()]

        fun fromApiCode(code: String): AllergenType? = byApiCode[code.uppercase()]
    }
}
