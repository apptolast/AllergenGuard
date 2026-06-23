package org.apptolast.menuadmin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apptolast.menuadmin.domain.model.Account
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountSession

/**
 * Holds the signed-in admin's resolved [AccountSession] (tenant + role). Repositories read it to scope
 * Firestore access to the current account. Set on login once the membership is resolved, cleared on
 * logout. Analogous to [SelectedRestaurantHolder].
 */
class CurrentAccountHolder {
    private val _session = MutableStateFlow<AccountSession?>(null)
    val session: StateFlow<AccountSession?> = _session.asStateFlow()

    // Whether the signed-in user is a platform owner (SUPER_ADMIN). Orthogonal to the account session:
    // a super-admin may also have their own account membership. Drives the Platform panel visibility.
    private val _isSuperAdmin = MutableStateFlow(false)
    val isSuperAdmin: StateFlow<Boolean> = _isSuperAdmin.asStateFlow()

    // When a super-admin is "managing" a tenant, this holds that account and [session] is an impersonated
    // ACCOUNT_ADMIN session for it; null otherwise. The owner's real session is stashed and restored on exit.
    private val _impersonatedAccount = MutableStateFlow<Account?>(null)
    val impersonatedAccount: StateFlow<Account?> = _impersonatedAccount.asStateFlow()
    private var ownSession: AccountSession? = null

    fun set(session: AccountSession) {
        _session.value = session
    }

    fun setSuperAdmin(value: Boolean) {
        _isSuperAdmin.value = value
    }

    /**
     * Super-admin enters "manage as account" mode: scoped screens then run as an ACCOUNT_ADMIN of
     * [account] (the scoped repos reload because the session changed). No-op for non-super-admins.
     */
    fun enterImpersonation(account: Account) {
        if (!_isSuperAdmin.value) return
        // Stash the owner's real session only the first time (so switching between accounts keeps it).
        if (_impersonatedAccount.value == null) ownSession = _session.value
        _impersonatedAccount.value = account
        _session.value = AccountSession(
            accountId = account.id,
            role = AccountRole.ACCOUNT_ADMIN,
            restaurantIds = emptyList(),
            region = account.region,
            language = account.language,
        )
    }

    /** Leaves "manage as account" mode and restores the owner's own session. */
    fun exitImpersonation() {
        if (_impersonatedAccount.value == null) return
        _session.value = ownSession
        ownSession = null
        _impersonatedAccount.value = null
    }

    fun clear() {
        _session.value = null
        _isSuperAdmin.value = false
        _impersonatedAccount.value = null
        ownSession = null
    }

    val accountIdOrNull: String? get() = _session.value?.accountId

    /** Current account id; throws if no session is resolved (a programming error in scoped repos). */
    fun requireAccountId(): String = _session.value?.accountId ?: error("No account session resolved")
}
