package com.apptolast.menufrontend

import androidx.compose.ui.window.ComposeUIViewController
import com.apptolast.menufrontend.di.initKoin

fun initKoinIos() {
    initKoin()
}

fun MainViewController() = ComposeUIViewController { App() }
