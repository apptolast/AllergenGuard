package com.apptolast.menufrontend.features.login.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.features.login.data.LoginAction
import com.apptolast.menufrontend.features.login.data.LoginState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.app_name
import com.apptolast.menufrontend.resources.ic_google
import com.apptolast.menufrontend.resources.login_apple_button
import com.apptolast.menufrontend.resources.login_button
import com.apptolast.menufrontend.resources.login_divider
import com.apptolast.menufrontend.resources.login_email_label
import com.apptolast.menufrontend.resources.login_email_placeholder
import com.apptolast.menufrontend.resources.login_forgot_password
import com.apptolast.menufrontend.resources.login_google_button
import com.apptolast.menufrontend.resources.login_have_account_prompt
import com.apptolast.menufrontend.resources.login_name_label
import com.apptolast.menufrontend.resources.login_password_label
import com.apptolast.menufrontend.resources.login_register_link
import com.apptolast.menufrontend.resources.login_register_prompt
import com.apptolast.menufrontend.resources.login_signin_link
import com.apptolast.menufrontend.resources.logo_app
import com.apptolast.menufrontend.resources.register_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoot(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(28.dp))

        // Logo
        Image(
            painter = painterResource(Res.drawable.logo_app),
            contentDescription = null,
            modifier = Modifier.size(280.dp),
        )

        // Title
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(40.dp))

        // Name field (sign-up only)
        if (state.isRegisterMode) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(LoginAction.NameChanged(it)) },
                label = { Text(stringResource(Res.string.login_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
            )
            Spacer(Modifier.height(16.dp))
        }

        // Email field
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.EmailChanged(it)) },
            label = { Text(stringResource(Res.string.login_email_label)) },
            placeholder = { Text(stringResource(Res.string.login_email_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        )

        Spacer(Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            label = { Text(stringResource(Res.string.login_password_label)) },
            singleLine = true,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.TogglePasswordVisibility) }) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        )

        // Forgot password (login only)
        if (!state.isRegisterMode) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.login_forgot_password),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onAction(LoginAction.ForgotPasswordClicked) },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }

        // Primary button: logs in, or creates the account in sign-up mode.
        Button(
            onClick = {
                onAction(
                    if (state.isRegisterMode) LoginAction.RegisterClicked else LoginAction.LoginClicked,
                )
            },
            enabled = !state.isLoading &&
                state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                (!state.isRegisterMode || state.name.isNotBlank()),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = stringResource(
                        if (state.isRegisterMode) Res.string.register_button else Res.string.login_button,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Social sign-in: Google on Android, Apple on iOS. The provider that isn't available on the
        // current platform is hidden, so the divider only shows when there is at least one button.
        if (state.isGoogleAvailable || state.isAppleAvailable) {
            Spacer(Modifier.height(24.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.login_divider),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
        }

        // Google sign in (Android)
        if (state.isGoogleAvailable) {
            OutlinedButton(
                onClick = { onAction(LoginAction.GoogleSignInClicked) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.login_google_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        // Sign in with Apple (iOS)
        if (state.isAppleAvailable) {
            if (state.isGoogleAvailable) Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onAction(LoginAction.AppleSignInClicked) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.login_apple_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Toggle between login and sign-up.
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    if (state.isRegisterMode) {
                        Res.string.login_have_account_prompt
                    } else {
                        Res.string.login_register_prompt
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    if (state.isRegisterMode) Res.string.login_signin_link else Res.string.login_register_link,
                ),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                // clickable before padding → the 8dp padding is part of the tap target.
                modifier = Modifier
                    .clickable { onAction(LoginAction.ToggleAuthMode) }
                    .padding(8.dp),
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}
