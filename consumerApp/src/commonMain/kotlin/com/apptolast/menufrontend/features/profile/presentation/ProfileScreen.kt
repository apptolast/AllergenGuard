package com.apptolast.menufrontend.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.User
import com.apptolast.menufrontend.features.components.BottomNavTab
import com.apptolast.menufrontend.features.components.BottomNavigationBar
import com.apptolast.menufrontend.features.components.allergenLabels
import com.apptolast.menufrontend.features.components.icon
import com.apptolast.menufrontend.features.profile.components.AllergenEditSheet
import com.apptolast.menufrontend.features.profile.components.SettingsItem
import com.apptolast.menufrontend.features.profile.data.ProfileAction
import com.apptolast.menufrontend.features.profile.data.ProfileState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.profile_allergies_description
import com.apptolast.menufrontend.resources.profile_edit
import com.apptolast.menufrontend.resources.profile_favorite_restaurants
import com.apptolast.menufrontend.resources.profile_help
import com.apptolast.menufrontend.resources.profile_language
import com.apptolast.menufrontend.resources.profile_language_es
import com.apptolast.menufrontend.resources.profile_logout
import com.apptolast.menufrontend.resources.profile_my_allergies
import com.apptolast.menufrontend.resources.profile_no_allergies
import com.apptolast.menufrontend.resources.profile_notifications
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoot(
    onLogout: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onLogout()
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onTabSelected = { tab ->
            when (tab) {
                BottomNavTab.EXPLORE -> onNavigateToExplore()
                BottomNavTab.FAVORITES -> onNavigateToFavorites()
                BottomNavTab.PROFILE -> { /* Already here */ }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    val user = state.user
    val allergenLabels = allergenLabels()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.PROFILE,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                val initials = user?.name
                    ?.split(" ")
                    ?.take(2)
                    ?.map { it.firstOrNull()?.uppercase() ?: "" }
                    ?.joinToString("") ?: ""
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Name & email
            Text(
                text = user?.name ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // My allergies section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.profile_my_allergies),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                TextButton(onClick = { onAction(ProfileAction.EditAllergiesClicked) }) {
                    Text(
                        text = stringResource(Res.string.profile_edit),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Saved allergen chips (read-only); tap Edit to change them
            if (state.allergens.isEmpty()) {
                Text(
                    text = stringResource(Res.string.profile_no_allergies),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Allergen.entries.filter { it in state.allergens }.forEach { allergen ->
                        AssistChip(
                            onClick = { onAction(ProfileAction.EditAllergiesClicked) },
                            label = {
                                Text(
                                    text = allergenLabels[allergen] ?: allergen.name,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = allergen.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.profile_allergies_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Settings
            SettingsItem(
                icon = Icons.Filled.Notifications,
                label = stringResource(Res.string.profile_notifications),
                onClick = { onAction(ProfileAction.NotificationsClicked) },
            )
            SettingsItem(
                icon = Icons.Filled.Language,
                label = stringResource(Res.string.profile_language),
                value = stringResource(Res.string.profile_language_es),
                onClick = { onAction(ProfileAction.LanguageClicked) },
            )
            SettingsItem(
                icon = Icons.Filled.FavoriteBorder,
                label = stringResource(Res.string.profile_favorite_restaurants),
                onClick = { onAction(ProfileAction.FavoriteRestaurantsClicked) },
            )
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = stringResource(Res.string.profile_help),
                onClick = { onAction(ProfileAction.HelpClicked) },
            )

            Spacer(Modifier.height(16.dp))

            // Logout
            TextButton(
                onClick = { onAction(ProfileAction.LogoutClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.profile_logout),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (state.isEditingAllergens) {
        AllergenEditSheet(
            selection = state.sheetSelection,
            allergenLabels = allergenLabels,
            isSaving = state.isSaving,
            onToggle = { onAction(ProfileAction.ToggleSheetAllergen(it)) },
            onSave = { onAction(ProfileAction.SaveAllergies) },
            onDismiss = { onAction(ProfileAction.DismissAllergenSheet) },
        )
    }
}

@Preview
@Composable
private fun PreviewProfileScreen() {
    AllergenGuardTheme {
        ProfileScreen(
            state = ProfileState(
                user = User(id = "1", name = "Alberto Hidalgo", email = "a@a.com"),
                allergens = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.PEANUTS, Allergen.DAIRY),
            ),
            onAction = {},
            onTabSelected = {},
        )
    }
}
