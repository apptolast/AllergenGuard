package org.apptolast.menuadmin.presentation.screens.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.mobile_favorites_empty_icon_description
import menuadmin.adminapp.generated.resources.mobile_favorites_empty_message
import menuadmin.adminapp.generated.resources.mobile_favorites_empty_title
import menuadmin.adminapp.generated.resources.mobile_favorites_title
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun MobileFavoritesScreen() {
    MobileFavoritesContent()
}

@Composable
fun MobileFavoritesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.mobile_favorites_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(Res.string.mobile_favorites_empty_icon_description),
                tint = MenuAdminTheme.colors.textMuted,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = stringResource(Res.string.mobile_favorites_empty_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.mobile_favorites_empty_message),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun MobileFavoritesContentPreview() {
    MenuAdminTheme {
        MobileFavoritesContent()
    }
}
