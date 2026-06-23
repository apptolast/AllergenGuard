package org.apptolast.menuadmin.di

import org.apptolast.menuadmin.domain.platform.EmailSender
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.domain.platform.MenuPdfExporter
import org.apptolast.menuadmin.platform.WebEmailSender
import org.apptolast.menuadmin.platform.WebFileHandler
import org.apptolast.menuadmin.platform.WebMenuPdfExporter
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<FileHandler> { WebFileHandler() }
        single<MenuPdfExporter> { WebMenuPdfExporter() }
        single<EmailSender> { WebEmailSender() }
    }
