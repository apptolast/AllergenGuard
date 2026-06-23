package org.apptolast.menuadmin.presentation.screens.platform

import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.platform.InvitationEmail

/**
 * Builds the [InvitationEmail] payload for a newly invited user. The role label and the email content
 * language follow the account's language (the EmailJS template localizes by the `language` param).
 * `loginUrl` is left empty: the web sender fills it from `window.location.origin` (the invitee logs
 * into the same admin app the super-admin is using).
 */
fun buildInvitationEmail(
    email: String,
    accountName: String,
    role: AccountRole,
    accountLanguage: String,
): InvitationEmail {
    val lang = if (accountLanguage.lowercase().startsWith("en")) "en" else "es"
    val roleLabel = when (role) {
        AccountRole.ACCOUNT_ADMIN -> if (lang == "en") "Account administrator" else "Administrador de cuenta"
        AccountRole.RESTAURANT_MANAGER -> if (lang == "en") "Restaurant manager" else "Gestor de restaurante"
    }
    return InvitationEmail(
        toEmail = email,
        accountName = accountName,
        roleLabel = roleLabel,
        loginUrl = "",
        language = lang,
    )
}
