package org.apptolast.menuadmin.domain.repository

interface WhitelistRepository {
    /**
     * True if [email] is on the admin registration whitelist. This client-side check gates the
     * sign-up UI; the real enforcement is server-side in the Firestore security rules.
     */
    suspend fun isWhitelisted(email: String): Boolean
}
