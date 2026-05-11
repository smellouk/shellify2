package dev.pwaforge.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import dev.pwaforge.core.engine.EngineType
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.model.LockType
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.presentation.webview.WebViewActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    geckoInstalled: Boolean,
    onAddApp: () -> Unit,
    onEditApp: (Long) -> Unit,
    onOpenApp: (WebApp) -> Unit,
    onOpenSettings: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    var hideDetails by remember { mutableStateOf(false) }

    val screenBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::setSearch,
                            placeholder = { Text("Search apps…") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text("PWAForge")
                    }
                },
                actions = {
                    IconButton(onClick = { hideDetails = !hideDetails }) {
                        Icon(if (hideDetails) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (hideDetails) "Show details" else "Hide details")
                    }
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) viewModel.setSearch("") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddApp,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add PWA")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Category filter chips
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") },
                        )
                    }
                    items(state.categories) { cat ->
                        val catColor = runCatching {
                            Color(android.graphics.Color.parseColor(cat.color))
                        }.getOrDefault(MaterialTheme.colorScheme.primary)
                        FilterChip(
                            selected = state.selectedCategoryId == cat.id,
                            onClick = { viewModel.selectCategory(cat.id) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(catColor, CircleShape),
                                    )
                                    Text(cat.name)
                                }
                            },
                        )
                    }
                }
            }

            if (state.apps.isEmpty() && !state.isLoading) {
                val emptyReason = state.categories
                    .find { it.id == state.selectedCategoryId }
                    ?.let { HomeEmptyState.FilteredCategory(it.name) }
                    ?: HomeEmptyState.NoApps
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    reason = emptyReason,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.apps, key = { it.id }) { app ->
                        AppCard(
                            app = app,
                            geckoInstalled = geckoInstalled,
                            categories = state.categories,
                            hideDetails = hideDetails,
                            onClick = { context.startActivity(WebViewActivity.launchIntent(context, app.id)) },
                            onEdit = { onEditApp(app.id) },
                            onSettings = { onOpenSettings(app.id) },
                            onDelete = { viewModel.delete(app) },
                            onClearData = { viewModel.clearData(app) },
                            onAssignCategory = { categoryId -> viewModel.assignCategory(app, categoryId) },
                        )
                    }
                }
            }
        }

    }

}

private sealed class HomeEmptyState {
    data object NoApps : HomeEmptyState()
    data class FilteredCategory(val name: String) : HomeEmptyState()
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, reason: HomeEmptyState = HomeEmptyState.NoApps) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (reason) {
                        is HomeEmptyState.NoApps -> Icons.Default.PhoneAndroid
                        is HomeEmptyState.FilteredCategory -> Icons.Default.Category
                    },
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 28.dp, y = (-16).dp)
                        .align(Alignment.TopEnd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            Text(
                when (reason) {
                    is HomeEmptyState.NoApps -> "No apps yet"
                    is HomeEmptyState.FilteredCategory -> "No apps in \"${reason.name}\""
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                when (reason) {
                    is HomeEmptyState.NoApps -> "Tap the button below to create your first app"
                    is HomeEmptyState.FilteredCategory -> "Apps assigned to this category will appear here"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AppCard(
    app: WebApp,
    geckoInstalled: Boolean,
    categories: List<Category>,
    hideDetails: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
    onClearData: () -> Unit,
    onAssignCategory: (Long?) -> Unit,
) {
    val engineMissing = app.engineType == EngineType.GECKOVIEW && !geckoInstalled
    var showMenu by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hideDetails) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.VisibilityOff, null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    AppIcon(app = app, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu",
                            modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { showMenu = false; onEdit() },
                        )
                        DropdownMenuItem(
                            text = { Text("Assign Category") },
                            leadingIcon = { Icon(Icons.Default.Category, null) },
                            onClick = { showMenu = false; showCategoryPicker = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = { showMenu = false; onSettings() },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Data") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                            onClick = { showMenu = false; showClearDataDialog = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { showMenu = false; onDelete() },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (hideDetails) "•".repeat(app.name.length.coerceIn(4, 12)) else app.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (hideDetails) "••••••••••••" else app.url.removePrefix("https://").removePrefix("http://"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FeatureTags(app)
            if (engineMissing) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = androidx.compose.ui.graphics.Color(0xFFFF9800),
                    )
                    Text(
                        "GeckoView required",
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                    )
                }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            currentCategoryId = app.categoryId,
            onDismiss = { showCategoryPicker = false },
            onSelect = { categoryId ->
                showCategoryPicker = false
                onAssignCategory(categoryId)
            },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Clear App Data") },
            text = { Text("This will delete all cookies, local storage, and cached data for \"${app.name}\". The app will stay in your list but you'll be logged out of all sessions.") },
            confirmButton = {
                TextButton(
                    onClick = { showClearDataDialog = false; onClearData() },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Clear Data") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<Category>,
    currentCategoryId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit,
) {
    var selected by remember { mutableStateOf(currentCategoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Category") },
        text = {
            Column {
                // "None" option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = null }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == null, onClick = { selected = null })
                    Spacer(Modifier.width(8.dp))
                    Text("None", style = MaterialTheme.typography.bodyLarge)
                }
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = cat.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == cat.id, onClick = { selected = cat.id })
                        Spacer(Modifier.width(8.dp))
                        Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selected) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun FeatureTags(app: WebApp) {
    data class Tag(val icon: androidx.compose.ui.graphics.vector.ImageVector, val desc: String, val color: Color)
    val tags = buildList {
        if (app.isFullscreen) add(Tag(Icons.Default.Fullscreen, "Fullscreen", Color(0xFFFB8C00)))
        if (app.adBlockEnabled) add(Tag(Icons.Default.Shield, "Ad block", Color(0xFF43A047)))
        if (app.translateEnabled) add(Tag(Icons.Default.GTranslate, "Translate", Color(0xFF1E88E5)))
        when (app.lockType) {
            LockType.PASSWORD -> add(Tag(Icons.Default.Lock, "Password lock", Color(0xFF7C4DFF)))
            LockType.SYSTEM   -> add(Tag(Icons.Default.Fingerprint, "System lock", Color(0xFF3F51B5)))
            LockType.NONE     -> Unit
        }
    }
    if (tags.isEmpty()) return
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(tag.color.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(tag.icon, contentDescription = tag.desc, modifier = Modifier.size(13.dp), tint = tag.color)
            }
        }
    }
}

@Composable
fun AppIcon(app: WebApp, modifier: Modifier = Modifier) {
    if (app.iconPath != null) {
        AsyncImage(
            model = app.iconPath,
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
        )
    } else {
        val color = runCatching { Color(android.graphics.Color.parseColor(app.themeColor)) }
            .getOrDefault(MaterialTheme.colorScheme.primary)
        Box(
            modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "P",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}
