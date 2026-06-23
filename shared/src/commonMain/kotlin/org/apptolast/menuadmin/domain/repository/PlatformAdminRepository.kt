package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.Account
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.model.Restaurant

/**
 * Platform-owner (SUPER_ADMIN) operations: CRUD of accounts and management of each account's users/roles.
 * Backed by cross-tenant Firestore access the security rules grant only to platform admins. The REST
 * client has no server-side query, so "users/restaurants of account X" are obtained by listing the whole
 * collection and filtering by `accountId` client-side (small volume).
 */
interface PlatformAdminRepository {
    /** Whether [uid] is a platform owner (exists in `platformAdmins`). */
    suspend fun isSuperAdmin(uid: String): Boolean

    fun getAccounts(): Flow<List<Account>>

    suspend fun getAccount(accountId: String): Account?

    /** Creates `accounts/{id}` (id derived from [name]); if [firstAdminEmail] is set, also creates its
     *  first ACCOUNT_ADMIN invitation so the account is ready to be managed. */
    suspend fun createAccount(
        name: String,
        region: String,
        language: String,
        firstAdminEmail: String?,
    ): Account

    suspend fun updateAccount(account: Account)

    /** Deletes the account and ALL its data: restaurants (+recipes/menus), ingredient catalog,
     *  memberships and invitations. Destructive — callers back up first and confirm. */
    suspend fun deleteAccountCascade(accountId: String)

    /** Users of the account: active members (with uid) + pending invitations (by email). */
    suspend fun getAccountUsers(accountId: String): List<AccountUser>

    /** Restaurants of the account (for the manager restaurant picker). */
    suspend fun getAccountRestaurants(accountId: String): List<Restaurant>

    /** Creates/updates the invitation for [email] in the account. */
    suspend fun inviteUser(
        accountId: String,
        email: String,
        role: AccountRole,
        restaurantIds: List<String>,
    )

    /** Updates an existing user's role/restaurants (membership if [uid] present, plus the invitation). */
    suspend fun updateUser(
        uid: String?,
        email: String,
        accountId: String,
        role: AccountRole,
        restaurantIds: List<String>,
    )

    /** Removes a user from the account (deletes the membership if [uid] present, and the invitation). */
    suspend fun removeUser(
        uid: String?,
        email: String,
    )

    /** JSON dump of the account's documents (path -> fields), taken before a destructive cascade delete. */
    suspend fun exportAccountBackup(accountId: String): String
}
