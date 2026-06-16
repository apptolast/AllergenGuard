package com.apptolast.menufrontend.di

import org.koin.core.module.Module

/**
 * Per-platform Koin bindings. Currently provides the platform
 * [com.apptolast.menufrontend.data.auth.SocialAuthClient] (Google on Android, Apple on iOS).
 */
expect val platformModule: Module
