package com.apptolast.menufrontend

import platform.Foundation.NSBundle

actual fun appVersion(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
