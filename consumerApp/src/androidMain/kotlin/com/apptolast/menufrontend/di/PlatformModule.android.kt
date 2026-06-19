package com.apptolast.menufrontend.di

import com.apptolast.menufrontend.data.auth.AndroidSocialAuthClient
import com.apptolast.menufrontend.data.auth.SocialAuthClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SocialAuthClient> { AndroidSocialAuthClient(androidContext()) }
}
