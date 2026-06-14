package org.apptolast.menuadmin.di

import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.platform.WebFileHandler
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<FileHandler> { WebFileHandler() }
    }
