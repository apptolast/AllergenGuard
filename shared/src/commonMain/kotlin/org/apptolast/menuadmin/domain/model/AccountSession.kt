package org.apptolast.menuadmin.domain.model

/**
 * The resolved tenant context of the signed-in admin: which account they belong to, their role within
 * it, and (for a [AccountRole.RESTAURANT_MANAGER]) which restaurants they may access. Drives data
 * scoping across the admin app. Resolved from `memberships/{uid}` + `accounts/{accountId}` on login.
 */
data class AccountSession(
    val accountId: String,
    val role: AccountRole,
    val restaurantIds: List<String> = emptyList(),
    val region: String = "EU",
    val language: String = "es",
) {
    val isAccountAdmin: Boolean get() = role == AccountRole.ACCOUNT_ADMIN

    /** Whether [restaurantId] is visible to this session (admins see all; managers only their assigned). */
    fun canSeeRestaurant(restaurantId: String): Boolean = isAccountAdmin || restaurantId in restaurantIds
}
