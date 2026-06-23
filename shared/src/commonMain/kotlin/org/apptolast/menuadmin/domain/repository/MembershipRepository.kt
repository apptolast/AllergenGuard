package org.apptolast.menuadmin.domain.repository

import org.apptolast.menuadmin.domain.model.AccountInvitation
import org.apptolast.menuadmin.domain.model.AccountSession

/**
 * Resolves the signed-in admin's tenant context. Replaces the binary whitelist gate: an invitation IS
 * the authorization, and a membership IS the resolved access.
 */
interface MembershipRepository {
    /** The user's resolved membership (account + role + account settings), or null if they have none. */
    suspend fun getMembership(uid: String): AccountSession?

    /** A pending invitation for [email] (account + role pre-authorised by the owner), or null. */
    suspend fun getInvitation(email: String): AccountInvitation?

    /**
     * Creates `memberships/{uid}` from an invitation on first login and returns the resolved session.
     * Security rules validate the written account/role/restaurantIds equal the invitation, so the client
     * cannot self-promote.
     */
    suspend fun materializeMembership(
        uid: String,
        invitation: AccountInvitation,
    ): AccountSession
}
