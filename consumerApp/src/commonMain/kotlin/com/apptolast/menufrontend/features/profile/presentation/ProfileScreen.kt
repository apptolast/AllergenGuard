package com.apptolast.menufrontend.features.profile.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.appVersion
import com.apptolast.menufrontend.core.share.APP_SHARE_URL
import com.apptolast.menufrontend.core.share.rememberShareLauncher
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.User
import com.apptolast.menufrontend.features.components.AllergenFilterChip
import com.apptolast.menufrontend.features.components.BottomNavTab
import com.apptolast.menufrontend.features.components.BottomNavigationBar
import com.apptolast.menufrontend.features.components.allergenLabels
import com.apptolast.menufrontend.features.profile.components.AllergenEditSheet
import com.apptolast.menufrontend.features.profile.components.DisclaimerSheet
import com.apptolast.menufrontend.features.profile.components.SettingsItem
import com.apptolast.menufrontend.features.profile.data.ProfileAction
import com.apptolast.menufrontend.features.profile.data.ProfileState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.action_cancel
import com.apptolast.menufrontend.resources.profile_allergen_disclaimer
import com.apptolast.menufrontend.resources.profile_allergies_description
import com.apptolast.menufrontend.resources.profile_delete_account
import com.apptolast.menufrontend.resources.profile_delete_confirm_button
import com.apptolast.menufrontend.resources.profile_delete_confirm_message
import com.apptolast.menufrontend.resources.profile_delete_confirm_title
import com.apptolast.menufrontend.resources.profile_edit
import com.apptolast.menufrontend.resources.profile_logout
import com.apptolast.menufrontend.resources.profile_logout_confirm_message
import com.apptolast.menufrontend.resources.profile_logout_confirm_title
import com.apptolast.menufrontend.resources.profile_my_allergies
import com.apptolast.menufrontend.resources.profile_no_allergies
import com.apptolast.menufrontend.resources.profile_share_app
import com.apptolast.menufrontend.resources.share_app_message
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
    val share = rememberShareLauncher()
    val shareMessage = stringResource(Res.string.share_app_message, APP_SHARE_URL)
    var showDisclaimer by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.PROFILE,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Allergen.entries.filter { it in state.allergens }.forEach { allergen ->
                        AllergenFilterChip(
                            allergen = allergen,
                            label = allergenLabels[allergen] ?: allergen.name,
                            isSelected = false,
                            onToggle = { onAction(ProfileAction.EditAllergiesClicked) },
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
                icon = Icons.Filled.Share,
                label = stringResource(Res.string.profile_share_app),
                onClick = { share(shareMessage) },
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                label = stringResource(Res.string.profile_allergen_disclaimer),
                onClick = { showDisclaimer = true },
            )

                Spacer(Modifier.height(24.dp))

                // Account actions. Sign out is the neutral, primary affordance (white surface so it
                // stands out from the gray canvas); "delete account" is an outlined danger button set
                // apart below it. Both require confirmation (dialogs further down) so neither fires on
                // an accidental tap.
                OutlinedButton(
                    onClick = { onAction(ProfileAction.LogoutClicked) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.profile_logout),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onAction(ProfileAction.DeleteAccountClicked) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.profile_delete_account),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            // App version pinned at the bottom, just above the bottom navigation bar.
            Text(
                text = "v${appVersion()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
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

    if (showDisclaimer) {
        DisclaimerSheet(onDismiss = { showDisclaimer = false })
    }

    // Logout confirmation (reversible action → simple confirm).
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(ProfileAction.DismissDialogs) },
            title = { Text(stringResource(Res.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(Res.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(ProfileAction.ConfirmLogout) }) {
                    Text(stringResource(Res.string.profile_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.DismissDialogs) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    // Delete-account confirmation (irreversible → warning icon, error-colored confirm, loading state).
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) onAction(ProfileAction.DismissDialogs) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text(stringResource(Res.string.profile_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.profile_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = { onAction(ProfileAction.ConfirmDeleteAccount) },
                    enabled = !state.isDeleting,
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.profile_delete_confirm_button),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(ProfileAction.DismissDialogs) },
                    enabled = !state.isDeleting,
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
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
