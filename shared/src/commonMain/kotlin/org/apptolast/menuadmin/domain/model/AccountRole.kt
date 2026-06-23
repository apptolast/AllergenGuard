package org.apptolast.menuadmin.domain.model

/**
 * Role of a user within an account (tenant). [ACCOUNT_ADMIN] sees every restaurant and the account's
 * ingredient catalog; [RESTAURANT_MANAGER] is scoped to the restaurants assigned in their membership.
 */
enum class AccountRole(
    val wire: String,
) {
    ACCOUNT_ADMIN("ACCOUNT_ADMIN"),
    RESTAURANT_MANAGER("RESTAURANT_MANAGER"),
    ;

    companion object {
        fun fromWire(value: String?): AccountRole? = entries.find { it.wire == value }
    }
}
