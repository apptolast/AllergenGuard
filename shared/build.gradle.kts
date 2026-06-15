import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.buildkonfig)
}

// Load local.properties for BuildKonfig (Firebase config, API base url, feature flag).
val localProperties: Properties by lazy {
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
        )
    }

    androidLibrary {
        namespace = "org.apptolast.menuadmin.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            // Browser-compatible Ktor engine (CIO pulls node:net and breaks the web bundle).
            implementation(libs.ktor.client.js)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            // Ktor HTTP client (REST data layer + Firebase Auth/Firestore REST)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            // Token storage
            implementation(libs.multiplatform.settings)
        }
    }
}

buildkonfig {
    packageName = "org.apptolast.menuadmin"
    defaultConfigs {
        buildConfigField(STRING, "API_BASE_URL", localProperties.getProperty("API_BASE_URL", ""))
        buildConfigField(STRING, "FIREBASE_API_KEY", localProperties.getProperty("FIREBASE_API_KEY", ""))
        buildConfigField(STRING, "FIREBASE_PROJECT_ID", localProperties.getProperty("FIREBASE_PROJECT_ID", ""))
        buildConfigField(STRING, "USE_FIRESTORE", localProperties.getProperty("USE_FIRESTORE", "true"))
    }
}
