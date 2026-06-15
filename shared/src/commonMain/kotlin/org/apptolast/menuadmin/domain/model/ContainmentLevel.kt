package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ContainmentLevel(
    val apiValue: String,
    val labelEs: String,
) {
    CONTAINS("CONTAINS", "Contiene"),
    MAY_CONTAIN("MAY_CONTAIN", "Puede contener"),
    FREE_OF("FREE_OF", "Libre de"),
    ;

    companion object {
        fun fromApi(value: String): ContainmentLevel =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: CONTAINS
    }
}
