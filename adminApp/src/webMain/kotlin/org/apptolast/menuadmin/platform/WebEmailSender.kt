package org.apptolast.menuadmin.platform

import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.config.BuildKonfig
import org.apptolast.menuadmin.domain.platform.EmailSender
import org.apptolast.menuadmin.domain.platform.InvitationEmail
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js

/**
 * Sends invitation emails from the browser with EmailJS (https://www.emailjs.com), vendored the same
 * way as jsPDF: `email.min.js` lives in webMain/resources and is loaded on demand (same-origin, no CDN).
 *
 * Uses only the EmailJS PUBLIC key — safe to ship in the client. The private/access key is never
 * embedded (it would grant unrestricted REST send). Config comes from [BuildKonfig] (local.properties
 * → CI secrets). Abuse is bounded by the EmailJS domain allowlist + quota cap configured in the dashboard.
 */
@OptIn(ExperimentalWasmJsInterop::class)
class WebEmailSender : EmailSender {
    override suspend fun sendInvitationEmail(invite: InvitationEmail) {
        val payload = Json.encodeToString(InvitationEmail.serializer(), invite)
        sendEmailJs(
            serviceId = BuildKonfig.EMAILJS_SERVICE_ID,
            templateId = BuildKonfig.EMAILJS_TEMPLATE_ID,
            publicKey = BuildKonfig.EMAILJS_PUBLIC_KEY,
            payloadJson = payload,
        ).await()
    }
}

/**
 * Loads `email.min.js` on demand (so the build stays untouched), maps the serialized [InvitationEmail]
 * to EmailJS template params and calls `emailjs.send`. The template's "To Email" field must be
 * `{{to_email}}` so the dynamic recipient is honored.
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun sendEmailJs(
    serviceId: String,
    templateId: String,
    publicKey: String,
    payloadJson: String,
): Promise<JsAny?> =
    js(
        """
        (function() {
            function ensureLib() {
                return new Promise(function(resolve, reject) {
                    if (window.emailjs) { resolve(); return; }
                    var existing = document.getElementById('emailjs-lib');
                    if (existing) {
                        existing.addEventListener('load', function() { resolve(); });
                        existing.addEventListener('error', function() { reject(new Error('No se pudo cargar email.min.js')); });
                        return;
                    }
                    // Vendored locally in webMain/resources -> served same-origin (no CDN/CORS/CSP).
                    var s = document.createElement('script');
                    s.id = 'emailjs-lib';
                    s.src = 'email.min.js';
                    s.onload = function() { resolve(); };
                    s.onerror = function() { reject(new Error('No se pudo cargar email.min.js')); };
                    document.head.appendChild(s);
                });
            }
            return ensureLib().then(function() {
                var data = JSON.parse(payloadJson);
                var params = {
                    to_email: data.toEmail,
                    account_name: data.accountName,
                    role: data.roleLabel,
                    login_url: data.loginUrl && data.loginUrl.length ? data.loginUrl : window.location.origin,
                    language: data.language
                };
                return window.emailjs.send(serviceId, templateId, params, { publicKey: publicKey });
            });
        })()
        """,
    )
