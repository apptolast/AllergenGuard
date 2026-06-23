package org.apptolast.menuadmin.domain.model

/**
 * A pending authorization for an email to join an account with a given role, provisioned manually by
 * the platform owner in `invitations/{email}`. On first login the client materializes its membership
 * from this (the security rules validate the match).
 */
data class AccountInvitation(
    val accountId: String,
    val role: AccountRole,
    val restaurantIds: List<String> = emptyList(),
)
