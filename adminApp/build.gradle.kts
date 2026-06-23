import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

// Admin app: WEB ONLY (wasmJs/js). Android & iOS targets live in :shared and will be consumed by
// the mobile consumer app (:consumerApp), not by this admin panel.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ktlint)
}

// Single source of truth for the admin platform version (exposed to code via BuildKonfig.APP_VERSION).
version = "1.4.1"

// Load local.properties for BuildKonfig (EmailJS client config: public key + service/template ids).
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

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
        }
    }
}

ktlint {
    android.set(false)
    outputToConsole.set(true)
    // Never lint generated sources (e.g. Compose resources `Res.kt`), which live
    // under a `/generated/` build directory. The predicate must not capture the
    // Gradle Project (keeps the configuration cache happy).
    filter {
        exclude { entry -> entry.file.path.contains("/generated/") }
    }
}

buildkonfig {
    packageName = "org.apptolast.menuadmin.config"
    defaultConfigs {
        buildConfigField(STRING, "APP_VERSION", project.version.toString())
        // EmailJS client config (PUBLIC key only — never the private/access key). Empty by default;
        // real values come from local.properties (dev) or CI secrets (see .github/workflows/ci-cd.yml).
        buildConfigField(STRING, "EMAILJS_PUBLIC_KEY", localProperties.getProperty("EMAILJS_PUBLIC_KEY", ""))
        buildConfigField(STRING, "EMAILJS_SERVICE_ID", localProperties.getProperty("EMAILJS_SERVICE_ID", ""))
        buildConfigField(STRING, "EMAILJS_TEMPLATE_ID", localProperties.getProperty("EMAILJS_TEMPLATE_ID", ""))
    }
}
