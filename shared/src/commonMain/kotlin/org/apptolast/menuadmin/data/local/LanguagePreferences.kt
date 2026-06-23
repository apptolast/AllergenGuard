package org.apptolast.menuadmin.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's in-app language choice (ES/EN) across launches, mirroring [ThemePreferences].
 *
 * A `null` value means "follow the device/system locale" (the default until the user picks one).
 * The app root observes [languageFlow] and feeds it to the platform `AppEnvironment` provider, which
 * overrides the Compose resource environment so `stringResource` resolves the selected language.
 */
class LanguagePreferences(
    private val settings: Settings = Settings(),
) {
    private val _language = MutableStateFlow(settings.getStringOrNull(KEY_LANGUAGE))
    val languageFlow: StateFlow<String?> = _language.asStateFlow()

    /** Current BCP-47 language tag ("es"/"en"), or `null` to follow the system locale. */
    val language: String? get() = _language.value

    fun setLanguage(value: String?) {
        if (value.isNullOrBlank()) settings.remove(KEY_LANGUAGE) else settings.putString(KEY_LANGUAGE, value)
        _language.value = value?.ifBlank { null }
    }

    private companion object {
        const val KEY_LANGUAGE = "app_language"
    }
}
