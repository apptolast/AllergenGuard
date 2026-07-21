package org.apptolast.menuadmin.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Spec 004 — iconografía de alérgenos: crustáceos ≠ moluscos.
 *
 * El bug: `CRUSTACEANS` usaba el codepoint 0xE4F7 = glifo `shell` (una concha), que parece un molusco.
 * El glifo de gamba `shrimp` vive en 0xE649 dentro del `lucide.ttf` incluido.
 */
class AllergenIconTest {
    // AC-04: cada alérgeno tiene un iconCode único (evita colisiones de glifo).
    @Test
    fun allIconCodesAreDistinct() {
        val codes = AllergenType.entries.map { it.iconCode }
        assertEquals(
            AllergenType.entries.size,
            codes.toSet().size,
            "Hay al menos dos alérgenos compartiendo iconCode",
        )
    }

    // AC-01: crustáceos usa el glifo `shrimp` (gamba), no `shell` (concha, que parecía un molusco).
    @Test
    fun crustaceansUsesShrimpGlyph() {
        assertEquals(0xE649, AllergenType.CRUSTACEANS.iconCode)
    }

    // AC-01: crustáceos y moluscos no comparten glifo.
    @Test
    fun crustaceansAndMollusksDiffer() {
        assertNotEquals(
            AllergenType.CRUSTACEANS.iconCode,
            AllergenType.MOLLUSKS.iconCode,
        )
    }
}
