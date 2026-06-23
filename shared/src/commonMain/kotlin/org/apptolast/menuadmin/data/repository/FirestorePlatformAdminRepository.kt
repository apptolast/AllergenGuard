package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.Account
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.model.AccountUserStatus
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.PlatformAdminRepository

/**
 * Firestore-backed [PlatformAdminRepository]. All cross-tenant access (listing accounts, memberships,
 * invitations across the whole project) is allowed by the security rules ONLY for platform admins; this
 * repo just performs the path-based reads/writes and filters by `accountId` client-side.
 */
class FirestorePlatformAdminRepository(
    private val firestore: FirestoreClient,
    private val json: Json,
) : PlatformAdminRepository {
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    private var hasLoaded = false

    override suspend fun isSuperAdmin(uid: String): Boolean = firestore.getDocument("$PLATFORM_ADMINS/$uid") != null

    override fun getAccounts(): Flow<List<Account>> =
        flow {
            if (!hasLoaded) runCatching { refreshAccounts() }
            emitAll(_accounts)
        }

    override suspend fun getAccount(accountId: String): Account? =
        firestore.getDocument("$ACCOUNTS/$accountId")?.toAccount()

    private suspend fun refreshAccounts() {
        _accounts.value = firestore.listDocuments(ACCOUNTS).map { it.toAccount() }.sortedBy { it.name.lowercase() }
        hasLoaded = true
    }

    override suspend fun createAccount(
        name: String,
        region: String,
        language: String,
        firstAdminEmail: String?,
    ): Account {
        val id = uniqueAccountId(name)
        firestore.patchDocument(
            "$ACCOUNTS/$id",
            mapOf("name" to name.trim(), "region" to region, "language" to language),
        )
        val email = firstAdminEmail?.trim()?.lowercase()
        if (!email.isNullOrBlank()) {
            firestore.patchDocument(
                "$INVITATIONS/$email",
                mapOf(
                    "accountId" to id,
                    "role" to AccountRole.ACCOUNT_ADMIN.wire,
                    "restaurantIds" to emptyList<String>(),
                ),
            )
        }
        refreshAccounts()
        return Account(id = id, name = name.trim(), region = region, language = language)
    }

    /** Derives a stable, readable id `acc_<slug>` from the name, appending a counter if it already exists. */
    private suspend fun uniqueAccountId(name: String): String {
        val slug = name.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "-").trim('-')
            .ifEmpty { "cuenta" }
        var id = "acc_$slug"
        var n = 2
        while (firestore.getDocument("$ACCOUNTS/$id") != null) {
            id = "acc_$slug-$n"
            n++
        }
        return id
    }

    override suspend fun updateAccount(account: Account) {
        firestore.patchDocument(
            "$ACCOUNTS/${account.id}",
            mapOf("name" to account.name.trim(), "region" to account.region, "language" to account.language),
        )
        refreshAccounts()
    }

    override suspend fun deleteAccountCascade(accountId: String) {
        // Restaurants of the account + their recipes/menus subtrees.
        val restaurants = firestore.listDocuments(RESTAURANTS).filter { it.accountId() == accountId }
        for (r in restaurants) {
            for (sub in SUBCOLLECTIONS) {
                firestore.listDocuments("$RESTAURANTS/${r.id}/$sub").forEach {
                    firestore.deleteDocument("$RESTAURANTS/${r.id}/$sub/${it.id}")
                }
            }
            firestore.deleteDocument("$RESTAURANTS/${r.id}")
        }
        // Account-private ingredient catalog.
        firestore.listDocuments("$ACCOUNTS/$accountId/ingredients").forEach {
            firestore.deleteDocument("$ACCOUNTS/$accountId/ingredients/${it.id}")
        }
        // Memberships + invitations of the account.
        firestore.listDocuments(MEMBERSHIPS).filter { it.accountId() == accountId }.forEach {
            firestore.deleteDocument("$MEMBERSHIPS/${it.id}")
        }
        firestore.listDocuments(INVITATIONS).filter { it.accountId() == accountId }.forEach {
            firestore.deleteDocument("$INVITATIONS/${it.id}")
        }
        // The account document itself.
        firestore.deleteDocument("$ACCOUNTS/$accountId")
        refreshAccounts()
    }

    override suspend fun getAccountUsers(accountId: String): List<AccountUser> {
        val members = firestore.listDocuments(MEMBERSHIPS)
            .filter { it.accountId() == accountId }
            .mapNotNull { it.toMemberUser() }
        val activeEmails = members.map { it.email.lowercase() }.toSet()
        val pending = firestore.listDocuments(INVITATIONS)
            .filter { it.accountId() == accountId && it.id.lowercase() !in activeEmails }
            .mapNotNull { it.toInvitationUser() }
        return (members + pending).sortedBy { it.email.lowercase() }
    }

    override suspend fun getAccountRestaurants(accountId: String): List<Restaurant> =
        firestore.listDocuments(RESTAURANTS)
            .filter { it.accountId() == accountId }
            .map {
                Restaurant(
                    id = it.id,
                    name = it.fields["name"] as? String ?: "",
                    slug = it.fields["slug"] as? String ?: it.id,
                )
            }
            .sortedBy { it.name.lowercase() }

    override suspend fun inviteUser(
        accountId: String,
        email: String,
        role: AccountRole,
        restaurantIds: List<String>,
    ) {
        val normalized = email.trim().lowercase()
        firestore.patchDocument(
            "$INVITATIONS/$normalized",
            mapOf(
                "accountId" to accountId,
                "role" to role.wire,
                "restaurantIds" to restaurantIds,
            ),
        )
    }

    override suspend fun updateUser(
        uid: String?,
        email: String,
        accountId: String,
        role: AccountRole,
        restaurantIds: List<String>,
    ) {
        // Live access: the membership (if the user already logged in).
        if (uid != null) {
            firestore.patchDocument(
                "$MEMBERSHIPS/$uid",
                mapOf("role" to role.wire, "restaurantIds" to restaurantIds),
                updateMask = listOf("role", "restaurantIds"),
            )
        }
        // Keep the invitation in sync (source for any future re-materialization).
        inviteUser(accountId, email, role, restaurantIds)
    }

    override suspend fun removeUser(
        uid: String?,
        email: String,
    ) {
        if (uid != null) firestore.deleteDocument("$MEMBERSHIPS/$uid")
        val normalized = email.trim().lowercase()
        if (normalized.isNotBlank()) firestore.deleteDocument("$INVITATIONS/$normalized")
    }

    override suspend fun exportAccountBackup(accountId: String): String {
        val docs = LinkedHashMap<String, Map<String, Any?>>()
        firestore.getDocument("$ACCOUNTS/$accountId")?.let { docs["$ACCOUNTS/$accountId"] = it.fields }
        firestore.listDocuments("$ACCOUNTS/$accountId/ingredients").forEach {
            docs["$ACCOUNTS/$accountId/ingredients/${it.id}"] = it.fields
        }
        firestore.listDocuments(RESTAURANTS).filter { it.accountId() == accountId }.forEach { r ->
            docs["$RESTAURANTS/${r.id}"] = r.fields
            for (sub in SUBCOLLECTIONS) {
                firestore.listDocuments("$RESTAURANTS/${r.id}/$sub").forEach {
                    docs["$RESTAURANTS/${r.id}/$sub/${it.id}"] = it.fields
                }
            }
        }
        firestore.listDocuments(MEMBERSHIPS).filter { it.accountId() == accountId }.forEach {
            docs["$MEMBERSHIPS/${it.id}"] = it.fields
        }
        firestore.listDocuments(INVITATIONS).filter { it.accountId() == accountId }.forEach {
            docs["$INVITATIONS/${it.id}"] = it.fields
        }
        val obj = buildJsonObject {
            put("accountId", JsonPrimitive(accountId))
            put(
                "documents",
                buildJsonObject { docs.forEach { (path, fields) -> put(path, anyToJson(fields)) } },
            )
        }
        return json.encodeToString(JsonObject.serializer(), obj)
    }

    // --- mappers -------------------------------------------------------------------------------------

    private fun FirestoreDocument.accountId(): String? = fields["accountId"] as? String

    private fun FirestoreDocument.restaurantIds(): List<String> =
        (fields["restaurantIds"] as? List<*>).orEmpty().mapNotNull { it as? String }

    private fun FirestoreDocument.toAccount(): Account =
        Account(
            id = id,
            name = fields["name"] as? String ?: id,
            region = fields["region"] as? String ?: "EU",
            language = fields["language"] as? String ?: "es",
        )

    private fun FirestoreDocument.toMemberUser(): AccountUser? {
        val role = AccountRole.fromWire(fields["role"] as? String) ?: return null
        return AccountUser(
            email = fields["email"] as? String ?: id, // falls back to uid if email not backfilled yet
            uid = id,
            role = role,
            restaurantIds = restaurantIds(),
            status = AccountUserStatus.ACTIVE,
        )
    }

    private fun FirestoreDocument.toInvitationUser(): AccountUser? {
        val role = AccountRole.fromWire(fields["role"] as? String) ?: return null
        return AccountUser(
            email = id,
            uid = null,
            role = role,
            restaurantIds = restaurantIds(),
            status = AccountUserStatus.PENDING,
        )
    }

    private fun anyToJson(v: Any?): JsonElement =
        when (v) {
            null -> JsonNull
            is String -> JsonPrimitive(v)
            is Boolean -> JsonPrimitive(v)
            is Int -> JsonPrimitive(v)
            is Long -> JsonPrimitive(v)
            is Double -> JsonPrimitive(v)
            is Map<*, *> -> buildJsonObject { v.forEach { (k, value) -> put(k.toString(), anyToJson(value)) } }
            is List<*> -> buildJsonArray { v.forEach { add(anyToJson(it)) } }
            else -> JsonPrimitive(v.toString())
        }

    private companion object {
        const val ACCOUNTS = "accounts"
        const val RESTAURANTS = "restaurants"
        const val MEMBERSHIPS = "memberships"
        const val INVITATIONS = "invitations"
        const val PLATFORM_ADMINS = "platformAdmins"
        val SUBCOLLECTIONS = listOf("recipes", "menus")
    }
}
