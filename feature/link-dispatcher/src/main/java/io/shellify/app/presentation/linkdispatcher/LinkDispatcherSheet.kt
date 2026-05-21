package io.shellify.app.presentation.linkdispatcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.shellify.app.domain.model.WebApp
import io.shellify.app.presentation.components.AppIcon
import io.shellify.app.presentation.theme.Dimens
import io.shellify.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDispatcherSheet(
    uiState: LinkDispatcherUiState,
    onAppSelected: (WebApp, String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (uiState.sheet is DispatchSheet.None) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLg)
                .padding(bottom = Dimens.spaceXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLg),
        ) {
            when (val sheet = uiState.sheet) {
                is DispatchSheet.Chooser -> ChooserContent(
                    sheet = sheet,
                    onAppSelected = onAppSelected,
                )
                is DispatchSheet.None -> Unit
            }
        }
    }
}

@Composable
private fun ChooserContent(
    sheet: DispatchSheet.Chooser,
    onAppSelected: (WebApp, String) -> Unit,
) {
    Text(
        text = stringResource(R.string.link_dispatcher_chooser_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    LazyColumn {
        items(sheet.matches) { app ->
            ChooserAppRow(app = app, onClick = { onAppSelected(app, sheet.url) })
        }
    }
}

@Composable
private fun ChooserAppRow(
    app: WebApp,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Dimens.sizeApp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            app = app,
            modifier = Modifier.size(Dimens.sizeApp),
        )
        Column {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = extractHost(app.url),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun extractHost(url: String): String =
    runCatching { java.net.URI(url).host }.getOrDefault(url)
