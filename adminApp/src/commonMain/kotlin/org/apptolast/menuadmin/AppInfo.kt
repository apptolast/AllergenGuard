package org.apptolast.menuadmin

import org.apptolast.menuadmin.config.BuildKonfig

/** Build info for the admin platform. Version comes from the Gradle `version` (see build.gradle.kts). */
object AppInfo {
    val VERSION: String = BuildKonfig.APP_VERSION
}
