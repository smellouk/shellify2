package dev.pwaforge.presentation.shortcuts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.pwaforge.R
import dev.pwaforge.presentation.theme.Dimens
import dev.pwaforge.presentation.home.AppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsScreen(viewModel: ShortcutsViewModel) {
    val state by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var isGridView by remember { mutableStateOf(true) }
    var iconPickItem by remember { mutableStateOf<ShortcutItem?>(null) }
    val pickImage = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        val item = iconPickItem ?: return@rememberLauncherForActivityResult
        iconPickItem = null
        if (uri != null) viewModel.applyPickedIcon(item, uri)
    }

    val screenBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shortcuts_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    if (state.items.isNotEmpty()) {
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.items.isNotEmpty() && state.addableApps.isNotEmpty()) {
                FloatingActionButton(
                    onClick = viewModel::showAddSheet,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(Dimens.spaceXs),
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.shortcuts_add_button))
                }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.items.isEmpty() -> EmptyState(
                onAddShortcut = viewModel::showAddSheet.takeIf { state.addableApps.isNotEmpty() },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(Dimens.sizeGridCell),
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(Dimens.spaceLg),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    ) {
                        items(state.items) { shortcutItem ->
                            ShortcutGridCard(
                                item = shortcutItem,
                                onRename = { viewModel.startRename(shortcutItem) },
                                onChangeIcon = {
                                    iconPickItem = shortcutItem
                                    pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                },
                                onRemove = { viewModel.showRemove(shortcutItem) },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(Dimens.spaceLg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                    ) {
                        items(state.items) { shortcutItem ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimens.cornerXl),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(Dimens.borderDefault, MaterialTheme.colorScheme.outlineVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                ShortcutRow(
                                    item = shortcutItem,
                                    onRename = { viewModel.startRename(shortcutItem) },
                                    onChangeIcon = {
                                        iconPickItem = shortcutItem
                                        pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                    },
                                    onRemove = { viewModel.showRemove(shortcutItem) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAddSheet) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissAddSheet) {
            Text(
                stringResource(R.string.shortcuts_add_button),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = Dimens.spaceLg, end = Dimens.spaceLg, bottom = Dimens.spaceSm),
            )
            LazyColumn {
                items(state.addableApps) { app ->
                    ListItem(
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(Dimens.sizeCard)
                                    .clip(RoundedCornerShape(Dimens.cornerLg))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(app = app, modifier = Modifier.size(Dimens.sizeApp))
                            }
                        },
                        headlineContent = {
                            Text(app.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Text(
                                app.url.removePrefix("https://").removePrefix("http://"),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier.clickable { viewModel.createShortcut(app) },
                    )
                }
                item { Spacer(Modifier.height(Dimens.spaceXl)) }
            }
        }
    }

    if (state.renameTarget != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRename,
            title = { Text(stringResource(R.string.shortcuts_rename_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = state.renameText,
                    onValueChange = viewModel::setRenameText,
                    label = { Text(stringResource(R.string.shortcuts_name_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmRename,
                    enabled = state.renameText.isNotBlank(),
                ) { Text(stringResource(R.string.shortcuts_rename_button)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRename) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    if (state.removeTarget != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRemove,
            icon = { Icon(Icons.Default.Delete, null) },
            title = { Text(stringResource(R.string.shortcuts_remove_dialog_title)) },
            text = {
                Text(stringResource(R.string.shortcuts_remove_body, state.removeTarget!!.label))
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.shortcuts_disable_button)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRemove) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Composable
private fun ShortcutRow(
    item: ShortcutItem,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
    onRemove: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(Dimens.sizeCard)
                    .clip(RoundedCornerShape(Dimens.cornerLg))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(app = item.app, modifier = Modifier.size(Dimens.sizeApp))
            }
        },
        headlineContent = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                item.app.url.removePrefix("https://").removePrefix("http://"),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.shortcuts_options_cd))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.shortcuts_menu_rename)) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showMenu = false; onRename() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.shortcuts_menu_change_icon)) },
                        leadingIcon = { Icon(Icons.Default.Image, null) },
                        onClick = { showMenu = false; onChangeIcon() },
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.shortcuts_menu_remove)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onRemove() },
                    )
                }
            }
        },
    )
}

@Composable
private fun ShortcutGridCard(
    item: ShortcutItem,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
    onRemove: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cornerXl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.borderDefault, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(Dimens.spaceMd)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app = item.app, modifier = Modifier.size(Dimens.sizeCard))
                Spacer(Modifier.width(Dimens.spaceMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.app.url.removePrefix("https://").removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(Dimens.sizeXl)) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.shortcuts_options_cd), modifier = Modifier.size(Dimens.sizeXs))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.shortcuts_menu_rename)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                                    onClick = { showMenu = false; onRename() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.shortcuts_menu_change_icon)) },
                                    leadingIcon = { Icon(Icons.Default.Image, null) },
                                    onClick = { showMenu = false; onChangeIcon() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.shortcuts_menu_remove)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { showMenu = false; onRemove() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAddShortcut: (() -> Unit)?, modifier: Modifier = Modifier) {
    val p97  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val p95  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    val p90  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
    val p40  = MaterialTheme.colorScheme.primary
    val surfDim = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.padding(horizontal = Dimens.size4xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Dimens.spaceXl + Dimens.spaceMd))

        // 160×160 illustration — 3 filled rings + single dashed orbit
        Box(modifier = Modifier.size(Dimens.illustrationSize), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(Dimens.illustrationSize).background(p97, CircleShape))
            Box(modifier = Modifier.size(Dimens.illustrationSizeMid).background(p95, CircleShape))
            Box(modifier = Modifier.size(Dimens.illustrationSizeInner).background(p90, CircleShape))
            Canvas(modifier = Modifier.size(Dimens.illustrationSize)) {
                drawCircle(
                    color = p40.copy(alpha = 0.35f),
                    radius = 70.dp.toPx(),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 7.dp.toPx()), 0f),
                    ),
                )
            }
            Box(
                modifier = Modifier.size(Dimens.sizeIllustrationTile).background(p40, RoundedCornerShape(Dimens.corner20)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Shortcut, null, modifier = Modifier.size(Dimens.sizeIconLarge), tint = Color.White)
            }
            Box(modifier = Modifier.size(Dimens.size2xl).offset(x = (-49).dp, y = (-57).dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)))
            Box(modifier = Modifier.size(Dimens.sizeLg).offset(x = 57.dp, y = (-45).dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)))
            Box(modifier = Modifier.size(Dimens.sizeLg).offset(x = (-61).dp, y = 51.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)))
            Box(modifier = Modifier.size(Dimens.size2xl).offset(x = 41.dp, y = 59.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.cornerSm))
                .border(Dimens.borderDefault, surfDim, RoundedCornerShape(Dimens.cornerSm)))
        }

        Spacer(Modifier.height(Dimens.space14))
        Text(
            stringResource(R.string.shortcuts_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = Dimens.letterSpacingTight,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(R.string.shortcuts_empty_desc),
            fontSize = Dimens.textSizeBody,
            lineHeight = Dimens.lineHeightBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onAddShortcut != null) {
            Spacer(Modifier.height(Dimens.space18))
            Button(
                onClick = onAddShortcut,
                shape = RoundedCornerShape(Dimens.corner24),
                modifier = Modifier.heightIn(min = Dimens.sizeApp),
                contentPadding = PaddingValues(horizontal = Dimens.sizeLg),
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(Dimens.sizeSm))
                Spacer(Modifier.size(Dimens.spaceSm))
                Text(stringResource(R.string.shortcuts_add_button), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.weight(1f))
    }
}
