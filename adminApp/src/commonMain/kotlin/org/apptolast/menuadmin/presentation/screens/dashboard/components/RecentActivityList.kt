package org.apptolast.menuadmin.presentation.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.ActivityEntry
import org.apptolast.menuadmin.domain.model.ActivityType
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import kotlin.time.Clock

@Composable
fun RecentActivityList(
    activities: List<ActivityEntry>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        activities.forEachIndexed { index, activity ->
            ActivityItem(activity = activity)
            if (index < activities.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(
    activity: ActivityEntry,
    modifier: Modifier = Modifier,
) {
    val dotColor = when (activity.type) {
        ActivityType.CREATED -> Green500
        ActivityType.UPDATED -> Blue500
        ActivityType.DELETED -> Red500
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = activity.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = formatTimestamp(activity.timestamp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatTimestamp(timestamp: kotlin.time.Instant): String {
    val now = Clock.System.now()
    val duration = now - timestamp
    val hours = duration.inWholeHours
    return when {
        hours < 1 -> "Hace ${duration.inWholeMinutes} min"
        hours < 24 -> "Hace $hours h"
        hours < 48 -> "Hace 1 dia"
        else -> "Hace ${hours / 24} dias"
    }
}

@Preview
@Composable
private fun RecentActivityListPreview() {
    MenuAdminTheme {
        RecentActivityList(
            activities = listOf(
                ActivityEntry(
                    description = "Nuevo ingrediente: Harina de trigo",
                    timestamp = Clock.System.now(),
                    type = ActivityType.CREATED,
                ),
                ActivityEntry(
                    description = "Receta actualizada: Croquetas",
                    timestamp = Clock.System.now(),
                    type = ActivityType.UPDATED,
                ),
            ),
        )
    }
}
