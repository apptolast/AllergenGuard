package org.apptolast.menuadmin.domain.model

/**
 * A tenant on the platform, managed by the SUPER_ADMIN (platform owner) from the Platform panel.
 * [id] is the Firestore document id (e.g. "acc_alacor"); [region]/[language] drive account-wide settings.
 */
data class Account(
    val id: String,
    val name: String,
    val region: String = "EU",
    val language: String = "es",
)
