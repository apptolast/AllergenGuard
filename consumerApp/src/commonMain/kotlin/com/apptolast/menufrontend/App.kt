package com.apptolast.menufrontend

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptolast.menufrontend.core.navigation.Navigation
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme

@Composable
fun App() {
    AllergenGuardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigation()
        }
    }
}
