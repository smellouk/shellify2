package io.shellify.app.presentation.onboarding

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.shellify.app.presentation.theme.Dimens
import io.shellify.core.ui.R

@Composable
fun UpdateConsentScreen(onAccepted: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceLg),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd),
        ) {
            Text(
                stringResource(R.string.consent_update_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.consent_update_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.spaceXxs))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(Dimens.spaceMd)) {
                    Text(
                        stringResource(R.string.consent_update_changes_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(Dimens.spaceXxs))
                    listOf(
                        stringResource(R.string.consent_update_changes_1),
                        stringResource(R.string.consent_update_changes_2),
                    ).forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = Dimens.spaceXxs),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = Dimens.spaceXxs)) {
                TextButton(onClick = { uriHandler.openUri("https://shellify.app/privacy.html") }) {
                    Text(stringResource(R.string.consent_read_privacy))
                }
                TextButton(onClick = { uriHandler.openUri("https://shellify.app/terms.html") }) {
                    Text(stringResource(R.string.consent_read_tos))
                }
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spaceLg, vertical = Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag("update_consent_checkbox"),
                )
                Text(
                    stringResource(R.string.consent_update_checkbox_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = onAccepted,
                enabled = checked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_consent_agree_button"),
            ) {
                Text(stringResource(R.string.consent_update_agree_button))
            }

            TextButton(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_consent_decline_button"),
            ) {
                Text(
                    stringResource(R.string.consent_decline_button),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
