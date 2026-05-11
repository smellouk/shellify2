package dev.pwaforge.presentation.settings

import android.os.Build
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.domain.model.UserAgentMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    viewModel: GlobalSettingsViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    var showUaDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val version = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
            .getOrDefault("—")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionLabel("Appearance")
            SettingsCard {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

            Spacer(Modifier.height(8.dp))
            SectionLabel("Data")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Clear all app data") },
                    supportingContent = { Text("Removes cookies and storage for all apps") },
                    trailingContent = {
                        IconButton(onClick = viewModel::showClearAllDialog) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("About")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Version") },
                    trailingContent = {
                        Text(
                            version,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }

    if (showUaDialog) {
        AlertDialog(
            onDismissRequest = { showUaDialog = false },
            title = { Text("Default user agent") },
            text = {
                Column {
                    @Suppress("DEPRECATION")
                    UserAgentMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setDefaultUaMode(mode); showUaDialog = false }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.defaultUaMode == mode,
                                onClick = { viewModel.setDefaultUaMode(mode); showUaDialog = false },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUaDialog = false }) { Text("Done") }
            },
        )
    }

    if (state.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissClearAllDialog,
            icon = { Icon(Icons.Default.DeleteSweep, null) },
            title = { Text("Clear all app data?") },
            text = { Text("This will delete cookies, local storage, and cached data for all apps. You will be logged out of all sessions.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::clearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissClearAllDialog) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) =
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )

@Composable
private fun SettingsCard(content: @Composable () -> Unit) =
    Card(elevation = CardDefaults.cardElevation(2.dp)) { content() }
