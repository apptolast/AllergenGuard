package org.apptolast.menuadmin

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.apptolast.menuadmin.di.initKoin
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
    ComposeViewport(document.body!!) {
        App()
    }
}
