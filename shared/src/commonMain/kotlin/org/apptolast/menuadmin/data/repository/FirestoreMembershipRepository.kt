package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.AccountInvitation
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountSession
import org.apptolast.menuadmin.domain.repository.MembershipRepository

/**
 * [MembershipRepository] backed by Firestore. Resolves `memberships/{uid}` (+ `accounts/{accountId}`
 * for region/language) and `invitations/{email}` by exact document id — no list/query needed (the REST
 * client has no query support). Membership materialization is a simple upsert validated by the rules.
 */
class FirestoreMembershipRepository(
    private val firestore: FirestoreClient,
) : MembershipRepository {
    override suspend fun getMembership(uid: String): AccountSession? {
        val doc = firestore.getDocument("$MEMBERSHIPS/$uid") ?: return null
        val accountId = doc.fields["accountId"] as? String ?: return null
        val role = AccountRole.fromWire(doc.fields["role"] as? String) ?: return null
        val (region, language) = loadAccountSettings(accountId)
        return AccountSession(
            accountId = accountId,
            role = role,
            restaurantIds = doc.restaurantIds(),
            region = region,
            language = language,
        )
    }

    override suspend fun getInvitation(email: String): AccountInvitation? {
        val doc = firestore.getDocument("$INVITATIONS/${email.trim().lowercase()}") ?: return null
        val accountId = doc.fields["accountId"] as? String ?: return null
        val role = AccountRole.fromWire(doc.fields["role"] as? String) ?: return null
        return AccountInvitation(accountId = accountId, role = role, restaurantIds = doc.restaurantIds())
    }

    override suspend fun materializeMembership(
        uid: String,
        email: String,
        invitation: AccountInvitation,
    ): AccountSession {
        // Always write restaurantIds (empty for admins) so it matches the invitation exactly: the
        // membership-create rule compares them with ==, which also stops a manager from self-granting
        // extra restaurants. `email` lets the platform panel list users by email (rules require it to
        // equal the verified token email).
        val fields = mapOf(
            "accountId" to invitation.accountId,
            "role" to invitation.role.wire,
            "restaurantIds" to invitation.restaurantIds,
            "email" to email.trim().lowercase(),
        )
        firestore.patchDocument("$MEMBERSHIPS/$uid", fields)
        return getMembership(uid) ?: AccountSession(
            accountId = invitation.accountId,
            role = invitation.role,
            restaurantIds = invitation.restaurantIds,
        )
    }

    /** Reads `(region, language)` for [accountId], falling back to EU/es if the account doc is missing. */
    private suspend fun loadAccountSettings(accountId: String): Pair<String, String> {
        val doc = firestore.getDocument("$ACCOUNTS/$accountId")
        val region = doc?.fields?.get("region") as? String ?: "EU"
        val language = doc?.fields?.get("language") as? String ?: "es"
        return region to language
    }

    private fun FirestoreDocument.restaurantIds(): List<String> =
        (fields["restaurantIds"] as? List<*>).orEmpty().mapNotNull { it as? String }

    private companion object {
        const val ACCOUNTS = "accounts"
        const val MEMBERSHIPS = "memberships"
        const val INVITATIONS = "invitations"
    }
}
