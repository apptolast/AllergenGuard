package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.apptolast.menuadmin.presentation.theme.Green500
import org.koin.compose.koinInject

enum class SnackbarType { ERROR, SUCCESS, INFO }

data class SnackbarMessage(
    val text: String,
    val type: SnackbarType = SnackbarType.ERROR,
)

/**
 * App-wide, one-shot snackbar bus. Anything (screens forwarding `uiState.error`, or callers directly)
 * posts a message here; the single [AppSnackbarHost] in `App` renders it, so error/success feedback is
 * consistent across the whole app instead of ad-hoc red text per screen.
 *
 * Registered as a Koin singleton; obtain it with `koinInject()`.
 */
class SnackbarController {
    private val _messages = Channel<SnackbarMessage>(Channel.BUFFERED)
    val messages: Flow<SnackbarMessage> = _messages.receiveAsFlow()

    fun showError(text: String) {
        _messages.trySend(SnackbarMessage(text, SnackbarType.ERROR))
    }

    fun showSuccess(text: String) {
        _messages.trySend(SnackbarMessage(text, SnackbarType.SUCCESS))
    }

    fun showInfo(text: String) {
        _messages.trySend(SnackbarMessage(text, SnackbarType.INFO))
    }
}

/**
 * Forwards a screen's `uiState.error` to the global snackbar whenever it becomes non-blank. Replaces
 * the old red error `Text` in each screen with a single call: `ErrorSnackbarEffect(uiState.error)`.
 */
@Composable
fun ErrorSnackbarEffect(error: String?) {
    val controller = koinInject<SnackbarController>()
    LaunchedEffect(error) {
        error?.takeIf { it.isNotBlank() }?.let(controller::showError)
    }
}

/**
 * The single snackbar host for the whole app. Place it once (in `App`) overlaying all content; it
 * collects [SnackbarController] messages and colours each one by [SnackbarType].
 */
@Composable
fun AppSnackbarHost(modifier: Modifier = Modifier) {
    val controller = koinInject<SnackbarController>()
    val hostState = remember { SnackbarHostState() }
    var type by remember { mutableStateOf(SnackbarType.ERROR) }

    LaunchedEffect(controller) {
        controller.messages.collect { message ->
            type = message.type
            // Replace any visible snackbar so the latest message is shown immediately.
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(
                message = message.text,
                withDismissAction = true,
                duration = if (message.type == SnackbarType.ERROR) {
                    SnackbarDuration.Long
                } else {
                    SnackbarDuration.Short
                },
            )
        }
    }

    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val container = when (type) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
            SnackbarType.SUCCESS -> Green500
            SnackbarType.INFO -> MaterialTheme.colorScheme.inverseSurface
        }
        val content = when (type) {
            SnackbarType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            SnackbarType.SUCCESS -> Color.White
            SnackbarType.INFO -> MaterialTheme.colorScheme.inverseOnSurface
        }
        Snackbar(
            snackbarData = data,
            containerColor = container,
            contentColor = content,
            actionColor = content,
            dismissActionContentColor = content,
            shape = RoundedCornerShape(10.dp),
        )
    }
}
