package io.shellify.app.presentation.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.shellify.app.R
import io.shellify.app.core.deeplink.DeepLinkHandler
import io.shellify.app.core.deeplink.QrCodeGenerator
import io.shellify.app.presentation.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShareSheet(
    appName: String,
    appUrl: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val customLink = remember(appUrl, appName) { DeepLinkHandler.buildCustomScheme(appUrl, appName) }
    val httpsLink = remember(appUrl, appName) { DeepLinkHandler.buildHttps(appUrl, appName) }
    val qrBitmap = remember(customLink) { QrCodeGenerator.generate(customLink) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLg)
                .padding(bottom = Dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd),
        ) {
            Text(appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
            ) {
                Text(customLink, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(httpsLink, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd)) {
                OutlinedButton(onClick = {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("Shellify link", customLink))
                }) { Text(stringResource(R.string.share_copy_link)) }
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$customLink\n\n$httpsLink")
                    }
                    context.startActivity(Intent.createChooser(intent, appName))
                }) { Text(stringResource(R.string.share_button)) }
            }
        }
    }
}
