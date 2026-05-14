package io.shellify.app.core.backup

interface BackupServiceProvider {
    val backupSettings: BackupSettings
    val backupManager: BackupManager
}
