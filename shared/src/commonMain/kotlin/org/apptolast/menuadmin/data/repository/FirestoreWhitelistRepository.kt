package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.domain.repository.WhitelistRepository

/**
 * Reads the admin registration whitelist from the Firestore `whitelist` collection, where each
 * allowed admin email is a document id (lowercased). The security rules expose `get` per-email
 * publicly, so the sign-up form can validate the address before the account is created.
 */
class FirestoreWhitelistRepository(
    private val firestore: FirestoreClient,
) : WhitelistRepository {
    override suspend fun isWhitelisted(email: String): Boolean =
        firestore.getDocument("whitelist/${email.trim().lowercase()}") != null
}
