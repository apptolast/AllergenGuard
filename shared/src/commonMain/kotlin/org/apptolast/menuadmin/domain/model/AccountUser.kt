package org.apptolast.menuadmin.domain.model

/**
 * A user attached to an account, as the SUPER_ADMIN sees them in the Platform panel. [status] is
 * [AccountUserStatus.ACTIVE] when the user already has a materialized membership ([uid] present) or
 * [AccountUserStatus.PENDING] when only an invitation exists (the user hasn't logged in yet, so no uid).
 */
data class AccountUser(
    val email: String,
    val uid: String?,
    val role: AccountRole,
    val restaurantIds: List<String> = emptyList(),
    val status: AccountUserStatus,
)

enum class AccountUserStatus { ACTIVE, PENDING }
