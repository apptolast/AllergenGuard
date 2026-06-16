package com.apptolast.menufrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.apptolast.menufrontend.data.auth.SocialAuthActivityHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Credential Manager (Google Sign-In) needs a foreground Activity to anchor its sheet.
        SocialAuthActivityHolder.attach(this)
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        SocialAuthActivityHolder.detach(this)
        super.onDestroy()
    }
}
