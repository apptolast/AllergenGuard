package org.apptolast.menuadmin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apptolast.menuadmin.domain.model.AccountSession

/**
 * Holds the signed-in admin's resolved [AccountSession] (tenant + role). Repositories read it to scope
 * Firestore access to the current account. Set on login once the membership is resolved, cleared on
 * logout. Analogous to [SelectedRestaurantHolder].
 */
class CurrentAccountHolder {
    private val _session = MutableStateFlow<AccountSession?>(null)
    val session: StateFlow<AccountSession?> = _session.asStateFlow()

    fun set(session: AccountSession) {
        _session.value = session
    }

    fun clear() {
        _session.value = null
    }

    val accountIdOrNull: String? get() = _session.value?.accountId

    /** Current account id; throws if no session is resolved (a programming error in scoped repos). */
    fun requireAccountId(): String = _session.value?.accountId ?: error("No account session resolved")
}
