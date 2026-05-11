package dev.pwaforge.presentation.settings

import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.pwaforge.core.backup.BackupSchedule
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.domain.model.UserAgentMode
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    viewModel: GlobalSettingsViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    var showUaDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val version = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
            .getOrDefault("—")
    }

    // SAF launchers
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.setBackupDirectory(it) }
    }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.showImportDialog(it) }
    }

    val screenBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    Scaffold(
        containerColor = screenBg,
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Appearance ────────────────────────────────────────────────────
            SectionLabel("Appearance")
            SettingsCard {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Theme", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val themeModes = listOf(
                        Triple(ThemeMode.SYSTEM, Icons.Default.BrightnessAuto, "System"),
                        Triple(ThemeMode.LIGHT,  Icons.Default.LightMode,      "Light"),
                        Triple(ThemeMode.DARK,   Icons.Default.DarkMode,        "Dark"),
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeModes.forEachIndexed { index, (mode, icon, label) ->
                            SegmentedButton(
                                selected = state.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, themeModes.size),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = state.themeMode == mode) {
                                        Icon(icon, null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                                    }
                                },
                            ) { Text(label) }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Dynamic colors") },
                        supportingContent = { Text("Follow wallpaper accent colors (Material You)") },
                        trailingContent = {
                            Switch(checked = state.dynamicColor, onCheckedChange = viewModel::setDynamicColor)
                        },
                    )
                }
            }

            // ── Browser ───────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionLabel("Browser")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Default user agent") },
                    supportingContent = { Text(state.defaultUaMode.label) },
                    trailingContent = {
                        TextButton(onClick = { showUaDialog = true }) { Text("Change") }
                    },
                )
            }

            // ── Security ──────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionLabel("Security")
            SettingsCard {
                if (state.hasPassword) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Lock, null) },
                        headlineContent = { Text("App password") },
                        supportingContent = { Text("Password is set") },
                        trailingContent = {
                            Row {
                                TextButton(onClick = viewModel::showChangePasswordDialog) { Text("Change") }
                                TextButton(
                                    onClick = viewModel::showRemovePasswordDialog,
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                ) { Text("Remove") }
                            }
                        },
                    )
                } else {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.LockOpen, null) },
                        headlineContent = { Text("App password") },
                        supportingContent = { Text("No password set") },
                        trailingContent = {
                            TextButton(onClick = viewModel::showSetPasswordDialog) { Text("Set") }
                        },
                    )
                }
            }

            // ── Backup ────────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionLabel("Backup")
            SettingsCard {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Backup, null) },
                    headlineContent = { Text("Encrypted backup") },
                    supportingContent = { Text(if (state.backupEnabled) "Enabled" else "Disabled") },
                    trailingContent = {
                        Switch(
                            checked = state.backupEnabled,
                            onCheckedChange = viewModel::setBackupEnabled,
                        )
                    },
                )
                AnimatedVisibility(
                    visible = state.backupEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        HorizontalDivider()

                        // Password
                        ListItem(
                            headlineContent = { Text("Backup password") },
                            supportingContent = {
                                Text(if (state.backupHasPassword) "Password is set" else "No password — required to backup")
                            },
                            trailingContent = {
                                TextButton(onClick = viewModel::showBackupPasswordDialog) {
                                    Text(if (state.backupHasPassword) "Change" else "Set")
                                }
                            },
                        )
                        HorizontalDivider()

                        // Directory
                        ListItem(
                            headlineContent = { Text("Backup folder") },
                            supportingContent = {
                                Text(
                                    state.backupDirectoryUri?.let { uriToDisplayName(it) }
                                        ?: "No folder selected"
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { folderPicker.launch(null) }) {
                                    Icon(Icons.Default.FolderOpen, "Select folder")
                                }
                            },
                        )
                        HorizontalDivider()

                        // Schedule
                        ListItem(
                            headlineContent = { Text("Auto-backup schedule") },
                            supportingContent = {
                                Text(when (state.backupSchedule) {
                                    BackupSchedule.NONE -> "Disabled"
                                    BackupSchedule.DAILY -> "Daily"
                                    BackupSchedule.WEEKLY -> "Weekly"
                                })
                            },
                            trailingContent = {
                                TextButton(onClick = { showScheduleDialog = true }) { Text("Change") }
                            },
                        )
                        if (state.backupLastTime > 0L) {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("Last backup") },
                                supportingContent = {
                                    Text(DateFormat.getMediumDateFormat(context).format(Date(state.backupLastTime)) +
                                        " " + DateFormat.getTimeFormat(context).format(Date(state.backupLastTime)))
                                },
                            )
                        }
                        HorizontalDivider()

                        // Actions
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = viewModel::backupNow,
                                enabled = !state.backupRunning,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (state.backupRunning) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Icon(Icons.Default.Backup, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Backup Now")
                            }
                            Button(
                                onClick = { filePicker.launch(arrayOf("application/octet-stream", "*/*")) },
                                enabled = !state.backupRunning,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                            ) {
                                Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Import Backup")
                            }
                        }
                    }
                }
            }

            // ── Data ──────────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionLabel("Data")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Clear all app data") },
                    supportingContent = { Text("Removes cookies and storage for all apps") },
                    trailingContent = {
                        IconButton(onClick = viewModel::showClearAllDialog) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Delete all apps") },
                    supportingContent = { Text("Removes all apps, their data and shortcuts") },
                    trailingContent = {
                        IconButton(onClick = viewModel::showDeleteAllAppsDialog) {
                            Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Delete all categories") },
                    supportingContent = { Text("Apps become uncategorized") },
                    trailingContent = {
                        IconButton(onClick = viewModel::showDeleteAllCategoriesDialog) {
                            Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Delete all shortcuts") },
                    supportingContent = { Text("Removes all launcher shortcuts from home screen") },
                    trailingContent = {
                        IconButton(onClick = viewModel::showDeleteAllShortcutsDialog) {
                            Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
            }

            // ── About ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            SectionLabel("About")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Version") },
                    trailingContent = {
                        Text(version, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                )
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showUaDialog) {
        AlertDialog(
            onDismissRequest = { showUaDialog = false },
            title = { Text("Default user agent") },
            text = {
                Column {
                    @Suppress("DEPRECATION")
                    UserAgentMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { viewModel.setDefaultUaMode(mode); showUaDialog = false }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = state.defaultUaMode == mode,
                                onClick = { viewModel.setDefaultUaMode(mode); showUaDialog = false })
                            Spacer(Modifier.width(8.dp))
                            Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showUaDialog = false }) { Text("Done") } },
        )
    }

    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Auto-backup schedule") },
            text = {
                Column {
                    BackupSchedule.entries.forEach { schedule ->
                        val label = when (schedule) {
                            BackupSchedule.NONE -> "Disabled"
                            BackupSchedule.DAILY -> "Daily"
                            BackupSchedule.WEEKLY -> "Weekly"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { viewModel.setBackupSchedule(schedule); showScheduleDialog = false }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = state.backupSchedule == schedule,
                                onClick = { viewModel.setBackupSchedule(schedule); showScheduleDialog = false })
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showScheduleDialog = false }) { Text("Done") } },
        )
    }

    if (state.showPasswordDialog) {
        PasswordDialog(
            mode = state.passwordDialogMode,
            onDismiss = viewModel::dismissPasswordDialog,
            onSet = { password -> viewModel.setPassword(password) },
            onChange = { current, new, onError -> viewModel.changePassword(current, new, onError) },
            onRemove = { current, onError -> viewModel.removePassword(current, onError) },
        )
    }

    if (state.showBackupPasswordDialog) {
        SinglePasswordDialog(
            title = "Backup password",
            description = "This password encrypts your backup file. Keep it safe — you'll need it to restore.",
            confirmLabel = "Save",
            onDismiss = viewModel::dismissBackupPasswordDialog,
            onConfirm = viewModel::setBackupPassword,
        )
    }

    if (state.showImportPasswordDialog) {
        SinglePasswordDialog(
            title = "Enter backup password",
            description = "Enter the password used when this backup was created.",
            confirmLabel = "Restore",
            onDismiss = viewModel::dismissImportDialog,
            onConfirm = viewModel::importBackup,
        )
    }

    if (state.showDeleteAllAppsDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteAllAppsDialog,
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Delete all apps?") },
            text = { Text("This will permanently delete all apps, their WebView data, and all launcher shortcuts. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAllApps,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDeleteAllAppsDialog) { Text("Cancel") } },
        )
    }

    if (state.showDeleteAllCategoriesDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteAllCategoriesDialog,
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Delete all categories?") },
            text = { Text("All categories will be removed. Apps will become uncategorized.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAllCategories,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDeleteAllCategoriesDialog) { Text("Cancel") } },
        )
    }

    if (state.showDeleteAllShortcutsDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteAllShortcutsDialog,
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Delete all shortcuts?") },
            text = { Text("All launcher shortcuts will be removed from your home screen. The apps themselves will remain.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteAllShortcuts,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDeleteAllShortcutsDialog) { Text("Cancel") } },
        )
    }

    if (state.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAllDialog,
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Clear all app data?") },
            text = { Text("This will delete cookies, local storage, and cached data for all apps. You will be logged out of all sessions.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear All") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissClearAllDialog) { Text("Cancel") } },
        )
    }

    state.backupResultMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearBackupMessage,
            icon = { Icon(Icons.Default.Backup, null) },
            title = { Text("Backup") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = viewModel::clearBackupMessage) { Text("OK") } },
        )
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
private fun SinglePasswordDialog(
    title: String,
    description: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, null) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(description, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { show = !show }) {
                            Icon(if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = null },
                    label = { Text("Confirm password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = error != null,
                    supportingText = { if (error != null) Text(error!!) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    password.length < 4 -> error = "At least 4 characters"
                    password != confirm -> error = "Passwords don't match"
                    else -> onConfirm(password)
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PasswordDialog(
    mode: PasswordDialogMode,
    onDismiss: () -> Unit,
    onSet: (String) -> Unit,
    onChange: (String, String, () -> Unit) -> Unit,
    onRemove: (String, () -> Unit) -> Unit,
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var currentError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }

    val title = when (mode) {
        PasswordDialogMode.SET -> "Set app password"
        PasswordDialogMode.CHANGE -> "Change app password"
        PasswordDialogMode.REMOVE -> "Remove app password"
    }

    fun validate(): Boolean {
        currentError = null; newError = null
        if (mode == PasswordDialogMode.REMOVE) {
            if (currentPassword.isBlank()) { currentError = "Enter your current password"; return false }
            return true
        }
        if (mode == PasswordDialogMode.CHANGE && currentPassword.isBlank()) {
            currentError = "Enter your current password"; return false
        }
        if (newPassword.length < 4) { newError = "At least 4 characters"; return false }
        if (newPassword != confirmPassword) { newError = "Passwords don't match"; return false }
        return true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, null) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (mode == PasswordDialogMode.CHANGE || mode == PasswordDialogMode.REMOVE) {
                    OutlinedTextField(
                        value = currentPassword, onValueChange = { currentPassword = it; currentError = null },
                        label = { Text("Current password") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showCurrent = !showCurrent }) {
                                Icon(if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        isError = currentError != null,
                        supportingText = { if (currentError != null) Text(currentError!!) },
                    )
                }
                if (mode == PasswordDialogMode.SET || mode == PasswordDialogMode.CHANGE) {
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it; newError = null },
                        label = { Text("New password") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showNew = !showNew }) {
                                Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        isError = newError != null,
                    )
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; newError = null },
                        label = { Text("Confirm password") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = newError != null,
                        supportingText = { if (newError != null) Text(newError!!) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!validate()) return@TextButton
                    when (mode) {
                        PasswordDialogMode.SET -> onSet(newPassword)
                        PasswordDialogMode.CHANGE -> onChange(currentPassword, newPassword) { currentError = "Wrong password" }
                        PasswordDialogMode.REMOVE -> onRemove(currentPassword) { currentError = "Wrong password" }
                    }
                },
                colors = if (mode == PasswordDialogMode.REMOVE)
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.textButtonColors(),
            ) { Text(if (mode == PasswordDialogMode.REMOVE) "Remove" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun uriToDisplayName(uriString: String): String = runCatching {
    val uri = android.net.Uri.parse(uriString)
    // SAF tree URI last path segment: "primary:Download/PWAForge" or "XXXX-XXXX:Backups"
    val docId = uri.lastPathSegment ?: return@runCatching uriString
    val colon = docId.indexOf(':')
    if (colon < 0) return@runCatching docId
    val volume = docId.substring(0, colon)
    val path = docId.substring(colon + 1)
    if (volume.equals("primary", ignoreCase = true)) "/storage/emulated/0/$path"
    else "/storage/$volume/$path"
}.getOrDefault(uriString)

@Composable
private fun SectionLabel(text: String) =
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp))

@Composable
private fun SettingsCard(content: @Composable () -> Unit) =
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) { content() }
