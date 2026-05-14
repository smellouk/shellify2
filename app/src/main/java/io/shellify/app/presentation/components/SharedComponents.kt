package io.shellify.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.shellify.app.R
import io.shellify.app.presentation.theme.Dimens

/**
 * Unified bordered card used in both the Add and Settings screens.
 * Provides only the card shell (shape, border, elevation, colors) — callers control inner layout.
 */
@Composable
internal fun SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerXl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(Dimens.borderDefault, MaterialTheme.colorScheme.outlineVariant),
        content = content,
    )
}

/**
 * Generic confirm/dismiss dialog.
 *
 * @param title    Dialog headline text.
 * @param body     Dialog body text.
 * @param confirmLabel  Label for the confirm button.
 * @param onConfirm Callback invoked when the user taps the confirm button.
 * @param onDismiss Callback invoked on dismiss or cancel.
 * @param icon     Optional leading icon shown above the title.
 * @param isDestructive When true, the confirm button is styled with [MaterialTheme.colorScheme.error].
 */
@Composable
internal fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = if (icon != null) ({ Icon(icon, null) }) else null,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                },
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

/**
 * Concentric-rings + dashed orbit + centre tile + ghost boxes illustration
 * reused across Home, Shortcuts, and Category empty-state screens.
 *
 * @param centerIcon Icon rendered in the centre tile of the illustration.
 * @param modifier   Optional modifier applied to the outer 160×160 [Box].
 */
@Composable
internal fun EmptyStateIllustration(
    centerIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val p97 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val p95 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    val p90 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
    val p40 = MaterialTheme.colorScheme.primary
    val surfDim = MaterialTheme.colorScheme.outlineVariant
    val surface = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier.size(Dimens.illustrationSize),
        contentAlignment = Alignment.Center,
    ) {
        // Outermost tinted ring
        Box(
            modifier = Modifier
                .size(Dimens.illustrationSize)
                .background(p97, CircleShape)
        )
        // Middle ring
        Box(
            modifier = Modifier
                .size(Dimens.illustrationSizeMid)
                .background(p95, CircleShape)
        )
        // Inner ring
        Box(
            modifier = Modifier
                .size(Dimens.illustrationSizeInner)
                .background(p90, CircleShape)
        )
        // Single dashed orbit at r=70
        Canvas(modifier = Modifier.size(Dimens.illustrationSize)) {
            drawCircle(
                color = p40.copy(alpha = 0.35f),
                radius = Dimens.illustrationRadius.toPx(),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(3.dp.toPx(), 7.dp.toPx()), 0f,
                    ),
                ),
            )
        }
        // Center tile: 64dp / radius 20dp / icon 30dp
        Box(
            modifier = Modifier
                .size(Dimens.sizeIllustrationTile)
                .background(p40, RoundedCornerShape(Dimens.corner20)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                centerIcon,
                null,
                modifier = Modifier.size(Dimens.sizeIconLarge),
                tint = Color.White,
            )
        }
        // Ghost tiles — positions match design handoff absolute coords
        Box(
            modifier = Modifier
                .size(Dimens.size2xl)
                .offset(x = (-49).dp, y = (-57).dp)
                .background(surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)),
        )
        Box(
            modifier = Modifier
                .size(Dimens.sizeLg)
                .offset(x = 57.dp, y = (-45).dp)
                .background(surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)),
        )
        Box(
            modifier = Modifier
                .size(Dimens.sizeLg)
                .offset(x = (-61).dp, y = 51.dp)
                .background(surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)),
        )
        Box(
            modifier = Modifier
                .size(Dimens.size2xl)
                .offset(x = 41.dp, y = 59.dp)
                .background(surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)),
        )
    }
}
