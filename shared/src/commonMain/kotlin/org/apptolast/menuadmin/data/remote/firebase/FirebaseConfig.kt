package org.apptolast.menuadmin.data.remote.firebase

import org.apptolast.menuadmin.BuildKonfig

/**
 * Central Firebase configuration + REST endpoints.
 *
 * The client talks to Firebase over REST (Identity Toolkit for Auth, Firestore REST for data)
 * so it works on every target including wasmJs, where the GitLive SDK is not available.
 */
object FirebaseConfig {
    val apiKey: String = BuildKonfig.FIREBASE_API_KEY
    val projectId: String = BuildKonfig.FIREBASE_PROJECT_ID

    /**
     * Feature flag: true = Firebase/Firestore data layer, false = legacy VPS backend. Exposed here
     * (public) because the generated [BuildKonfig] is internal to :shared and not visible to apps.
     */
    val useFirestore: Boolean = BuildKonfig.USE_FIRESTORE.toBoolean()

    const val IDENTITY_TOOLKIT = "https://identitytoolkit.googleapis.com/v1"
    const val SECURE_TOKEN = "https://securetoken.googleapis.com/v1"
    const val FIRESTORE = "https://firestore.googleapis.com/v1"
    const val STORAGE = "https://firebasestorage.googleapis.com"

    /** Base path for documents in the default Firestore database. */
    val firestoreDocuments: String
        get() = "$FIRESTORE/projects/$projectId/databases/(default)/documents"

    /** Default Cloud Storage bucket for this Firebase project. */
    val storageBucket: String
        get() = "$projectId.firebasestorage.app"
}
