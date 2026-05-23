package io.shellify.app.presentation.settings.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.presentation.components.EmptyStateIllustration
import io.shellify.app.presentation.theme.Dimens
import io.shellify.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    viewModel: NotificationHistoryViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    NotificationHistoryContent(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryContent(
    state: NotificationHistoryUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_notifications_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.notifications.isEmpty() -> {
                EmptyState(modifier = Modifier.padding(innerPadding))
            }
            else -> {
                NotificationList(
                    notifications = state.notifications,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.spaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyStateIllustration(
            centerIcon = Icons.Default.Notifications,
            modifier = Modifier.padding(top = Dimens.spaceXxl),
        )
        Text(
            text = stringResource(R.string.settings_notifications_history_empty_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = Dimens.spaceLg),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.settings_notifications_history_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.spaceSm),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NotificationList(
    notifications: List<PwaNotification>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(notifications) { index, notification ->
            NotificationRow(notification = notification)
            if (index < notifications.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Dimens.spaceLg),
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.settings_notifications_history_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceMd),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NotificationRow(notification: PwaNotification) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        notification.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()

    ListItem(
        headlineContent = { Text(notification.title) },
        supportingContent = notification.body?.let { body ->
            { Text(body, maxLines = 2) }
        },
        trailingContent = {
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
