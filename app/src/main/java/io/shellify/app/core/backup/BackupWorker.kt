package io.shellify.app.core.backup

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.shellify.app.ShellifyApplication
import kotlinx.coroutines.flow.first

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ShellifyApplication
        val settings = app.backupSettings
        val manager = app.backupManager

        val password = settings.getPassword() ?: return Result.failure()
        val uriString = settings.directoryUri.first() ?: return Result.failure()

        val uri = runCatching { Uri.parse(uriString) }.getOrElse { return Result.failure() }

        return manager.backup(password, uri).fold(
            onSuccess = {
                settings.setLastBackupTime(System.currentTimeMillis())
                Result.success()
            },
            onFailure = { Result.retry() },
        )
    }
}
