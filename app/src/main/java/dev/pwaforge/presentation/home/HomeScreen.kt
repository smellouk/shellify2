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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.fragment.app.FragmentActivity
import dev.pwaforge.core.security.showSystemLockPrompt
import dev.pwaforge.core.security.verifyPassword
import dev.pwaforge.domain.model.Category
import dev.pwaforge.domain.model.LockType
import dev.pwaforge.domain.model.WebApp
import dev.pwaforge.presentation.webview.WebViewActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddApp: () -> Unit,
    onEditApp: (Long) -> Unit,
    onOpenApp: (WebApp) -> Unit,
    onOpenSettings: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val globalPasswordHash by viewModel.globalPasswordHash.collectAsState()
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    var lockedApp by remember { mutableStateOf<WebApp?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var showPasswordInput by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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
                EmptyState(modifier = Modifier.fillMaxSize())
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
                            categories = state.categories,
                            onClick = {
                                when (app.lockType) {
                                    LockType.NONE -> context.startActivity(WebViewActivity.launchIntent(context, app.id))
                                    LockType.PASSWORD -> {
                                        lockedApp = app
                                        passwordInput = ""
                                        showPasswordInput = false
                                        passwordError = null
                                    }
                                    LockType.SYSTEM -> {
                                        val activity = context as? FragmentActivity ?: return@AppCard
                                        showSystemLockPrompt(
                                            activity = activity,
                                            title = "Open ${app.name}",
                                            onSuccess = { context.startActivity(WebViewActivity.launchIntent(context, app.id)) },
                                            onFailed = {},
                                        )
                                    }
                                }
                            },
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

    lockedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { lockedApp = null },
            icon = { Icon(Icons.Default.Lock, null) },
            title = { Text(app.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Enter password to open this app",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; passwordError = null },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPasswordInput) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPasswordInput = !showPasswordInput }) {
                                Icon(if (showPasswordInput) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        isError = passwordError != null,
                        supportingText = { if (passwordError != null) Text(passwordError!!) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val hash = globalPasswordHash
                    if (hash != null && verifyPassword(passwordInput, hash)) {
                        lockedApp = null
                        context.startActivity(WebViewActivity.launchIntent(context, app.id))
                    } else {
                        passwordError = "Wrong password"
                    }
                }) { Text("Open") }
            },
            dismissButton = {
                TextButton(onClick = { lockedApp = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
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
                    imageVector = Icons.Default.PhoneAndroid,
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
                "No apps yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                "Tap the button below to create your first app",
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
    categories: List<Category>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
    onClearData: () -> Unit,
    onAssignCategory: (Long?) -> Unit,
) {
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
                AppIcon(app = app, modifier = Modifier.size(48.dp))
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
            Text(app.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                app.url.removePrefix("https://").removePrefix("http://"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
