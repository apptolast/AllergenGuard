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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.theme.AllergenActiveBg
import com.apptolast.menufrontend.core.theme.AllergenActiveText
import com.apptolast.menufrontend.core.theme.DangerRed
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.components.BottomNavTab
import com.apptolast.menufrontend.features.components.BottomNavigationBar
import com.apptolast.menufrontend.features.profile.components.SettingsItem
import com.apptolast.menufrontend.features.profile.data.ProfileAction
import com.apptolast.menufrontend.features.profile.data.ProfileState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.allergen_dairy
import com.apptolast.menufrontend.resources.allergen_eggs
import com.apptolast.menufrontend.resources.allergen_fish
import com.apptolast.menufrontend.resources.allergen_gluten
import com.apptolast.menufrontend.resources.allergen_peanuts
import com.apptolast.menufrontend.resources.allergen_soy
import com.apptolast.menufrontend.resources.profile_allergies_description
import com.apptolast.menufrontend.resources.profile_edit
import com.apptolast.menufrontend.resources.profile_favorite_restaurants
import com.apptolast.menufrontend.resources.profile_help
import com.apptolast.menufrontend.resources.profile_language
import com.apptolast.menufrontend.resources.profile_language_es
import com.apptolast.menufrontend.resources.profile_logout
import com.apptolast.menufrontend.resources.profile_my_allergies
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

    val allergenLabels = mapOf(
        Allergen.GLUTEN to stringResource(Res.string.allergen_gluten),
        Allergen.FISH to stringResource(Res.string.allergen_fish),
        Allergen.PEANUTS to stringResource(Res.string.allergen_peanuts),
        Allergen.DAIRY to stringResource(Res.string.allergen_dairy),
        Allergen.EGGS to stringResource(Res.string.allergen_eggs),
        Allergen.SOY to stringResource(Res.string.allergen_soy),
    )

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
                TextButton(onClick = { }) {
                    Text(
                        text = stringResource(Res.string.profile_edit),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Allergen chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allergenLabels.forEach { (allergen, label) ->
                    val isSelected = user?.allergens?.contains(allergen) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAction(ProfileAction.ToggleAllergen(allergen)) },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AllergenActiveBg,
                            selectedLabelColor = AllergenActiveText,
                        ),
                    )
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
                    color = DangerRed,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
