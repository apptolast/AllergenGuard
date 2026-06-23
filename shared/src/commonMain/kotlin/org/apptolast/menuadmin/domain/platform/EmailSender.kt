package org.apptolast.menuadmin.domain.platform

import kotlinx.serialization.Serializable

/**
 * Sends transactional emails. The only implementation today is web (EmailJS, client-side); the
 * interface lets a future server-side sender (Cloud Function) be swapped in without touching callers.
 */
interface EmailSender {
    suspend fun sendInvitationEmail(invite: InvitationEmail)
}

/** Data shown in the invitation email; serialized to JSON and mapped to the EmailJS template params. */
@Serializable
data class InvitationEmail(
    val toEmail: String,
    val accountName: String,
    val roleLabel: String,
    val loginUrl: String,
    val language: String,
)
