package org.apptolast.menuadmin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
