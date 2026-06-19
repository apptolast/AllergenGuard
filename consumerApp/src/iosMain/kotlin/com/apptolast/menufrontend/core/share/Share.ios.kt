package com.apptolast.menufrontend.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberShareLauncher(): (String) -> Unit = remember {
    { text ->
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        // Present from the top-most view controller so the sheet shows even over modals.
        var presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (presenter?.presentedViewController != null) {
            presenter = presenter.presentedViewController
        }
        presenter?.presentViewController(controller, animated = true, completion = null)
    }
}
