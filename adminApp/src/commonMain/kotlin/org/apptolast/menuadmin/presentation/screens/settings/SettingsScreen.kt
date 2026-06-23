package org.apptolast.menuadmin.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.settings_dark_theme
import menuadmin.adminapp.generated.resources.settings_dark_theme_desc
import menuadmin.adminapp.generated.resources.settings_language
import menuadmin.adminapp.generated.resources.settings_language_desc
import menuadmin.adminapp.generated.resources.settings_language_english
import menuadmin.adminapp.generated.resources.settings_language_spanish
import menuadmin.adminapp.generated.resources.settings_language_system
import menuadmin.adminapp.generated.resources.settings_subtitle
import menuadmin.adminapp.generated.resources.settings_title
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onToggleDarkTheme = viewModel::onToggleDarkTheme,
        onSelectLanguage = viewModel::onSelectLanguage,
        onDismissMessage = viewModel::dismissMessage,
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSelectLanguage: (String?) -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            Text(
                text = stringResource(Res.string.settings_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.settings_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ErrorSnackbarEffect(uiState.error)
        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MenuAdminTheme.colors.success, fontSize = 13.sp)
        }

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.settings_dark_theme),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.settings_dark_theme_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = onToggleDarkTheme,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        SettingsCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.settings_language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.settings_language_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageOption(
                        label = stringResource(Res.string.settings_language_system),
                        selected = uiState.language == null,
                        onClick = { onSelectLanguage(null) },
                    )
                    LanguageOption(
                        label = stringResource(Res.string.settings_language_spanish),
                        selected = uiState.language == "es",
                        onClick = { onSelectLanguage("es") },
                    )
                    LanguageOption(
                        label = stringResource(Res.string.settings_language_english),
                        selected = uiState.language == "en",
                        onClick = { onSelectLanguage("en") },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

@Preview
@Composable
private fun SettingsContentPreview() {
    MenuAdminTheme {
        SettingsContent(
            uiState = SettingsUiState(language = "es"),
            onToggleDarkTheme = {},
            onSelectLanguage = {},
            onDismissMessage = {},
        )
    }
}
