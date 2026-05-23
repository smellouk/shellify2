package io.shellify.app.presentation.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.shellify.core.ui.R

@Suppress("MagicNumber")
@Composable
fun DndRangeRow(
    startHour: Int,
    endHour: Int,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    val isSet = startHour != -1 || endHour != -1
    val rangeText = when {
        startHour != -1 && endHour != -1 -> "%02d:00–%02d:00".format(startHour, endHour)
        startHour != -1 -> "%02d:00–?".format(startHour)
        endHour != -1 -> "?–%02d:00".format(endHour)
        else -> stringResource(R.string.settings_notifications_dnd_not_set)
    }
    ListItem(
        leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
        headlineContent = {
            Text(
                text = stringResource(R.string.settings_notifications_dnd),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClick) {
                    Text(
                        text = rangeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSet) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                if (isSet) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.settings_notifications_dnd_clear_cd),
                        )
                    }
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
