package org.apptolast.menuadmin.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        appDeclaration?.invoke(this)
        modules(dataModule, presentationModule, platformModule())
    }
}
