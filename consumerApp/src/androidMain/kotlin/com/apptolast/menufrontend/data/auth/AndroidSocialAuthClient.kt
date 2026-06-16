package com.apptolast.menufrontend.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.apptolast.menufrontend.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the foreground Activity so Credential Manager can anchor its bottom
 * sheet. Set/cleared from `MainActivity.onCreate/onDestroy`.
 */
object SocialAuthActivityHolder {
    private var activityRef: WeakReference<Activity>? = null

    fun attach(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun detach(activity: Activity) {
        if (activityRef?.get() === activity) activityRef = null
    }

    fun requireActivity(): Activity = activityRef?.get()
        ?: throw SocialAuthUnavailableException("No hay una pantalla activa para iniciar sesión.")
}

/**
 * Android social sign-in: Google via the modern Credential Manager + `GetSignInWithGoogleOption`
 * (the explicit "Sign in with Google" button flow). Returns only the Google **ID token**; the
 * Firebase exchange happens in the shared REST layer.
 *
 * The `serverClientId` must be the **Web** OAuth client (`client_type: 3` in `google-services.json`),
 * provided via `BuildConfig.GOOGLE_WEB_CLIENT_ID` (see `local.properties` → `GOOGLE_WEB_CLIENT_ID`).
 */
class AndroidSocialAuthClient(
    appContext: Context,
) : SocialAuthClient {
    private val credentialManager = CredentialManager.create(appContext)
    private val webClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    override val isGoogleAvailable: Boolean = webClientId.isNotBlank()
    override val isAppleAvailable: Boolean = false

    override suspend fun signInWithGoogle(): SocialSignInResult {
        if (!isGoogleAvailable) {
            throw SocialAuthUnavailableException("Google Sign-In no está configurado (falta GOOGLE_WEB_CLIENT_ID).")
        }
        val activity = SocialAuthActivityHolder.requireActivity()
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val response = try {
            credentialManager.getCredential(context = activity, request = request)
        } catch (_: GetCredentialCancellationException) {
            throw SocialAuthCancelledException()
        } catch (e: GetCredentialException) {
            throw SocialAuthUnavailableException(e.message ?: "No se pudo iniciar sesión con Google.")
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            return SocialSignInResult(
                providerId = "google.com",
                idToken = google.idToken,
                displayName = google.displayName,
            )
        }
        throw SocialAuthUnavailableException("No se obtuvo una credencial de Google válida.")
    }

    override suspend fun signInWithApple(): SocialSignInResult =
        throw SocialAuthUnavailableException("Sign in with Apple solo está disponible en iOS.")
}
