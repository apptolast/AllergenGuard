package org.apptolast.menuadmin.data.remote.firebase

import org.apptolast.menuadmin.BuildKonfig
import org.apptolast.menuadmin.data.remote.firebase.FirebaseConfig.databaseId

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

    /**
     * Named Firestore database to target. Release/prod reuses the original `(default)` database; debug
     * builds point at a separate `debug` database. Defaults to the build-time [BuildKonfig] value (set via
     * the `FIRESTORE_DATABASE_ID` Gradle property / local.properties), but an app may override it at
     * startup before any Firestore call — e.g. the consumer Android app sets it from `BuildConfig.DEBUG`.
     * The REST API accepts the literal id `(default)` in the path.
     */
    var databaseId: String = BuildKonfig.FIRESTORE_DATABASE_ID

    const val IDENTITY_TOOLKIT = "https://identitytoolkit.googleapis.com/v1"
    const val SECURE_TOKEN = "https://securetoken.googleapis.com/v1"
    const val FIRESTORE = "https://firestore.googleapis.com/v1"
    const val STORAGE = "https://firebasestorage.googleapis.com"

    /** Base path for documents in the active Firestore database ([databaseId]). */
    val firestoreDocuments: String
        get() = "$FIRESTORE/projects/$projectId/databases/$databaseId/documents"

    /** Default Cloud Storage bucket for this Firebase project. */
    val storageBucket: String
        get() = "$projectId.firebasestorage.app"
}
