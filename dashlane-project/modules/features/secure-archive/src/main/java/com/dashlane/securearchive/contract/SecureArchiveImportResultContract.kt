package com.dashlane.securearchive.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.securearchive.BackupActivityIntents

class SecureArchiveImportResultContract : ActivityResultContract<Uri, BackupActivityResult>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return BackupActivityIntents.newImportIntent(
            context = context,
            uri = input,
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): BackupActivityResult {
        val isSuccessful = intent?.getBooleanExtra(BackupActivityIntents.EXTRA_IS_SUCCESSFUL, false) ?: false
        val isShared = intent?.getBooleanExtra(BackupActivityIntents.EXTRA_SHARED, false) ?: false
        val count = intent?.getIntExtra(BackupActivityIntents.EXTRA_COUNT, 0) ?: 0
        return BackupActivityResult(
            isSuccessful = isSuccessful,
            isShared = isShared,
            count = count,
        )
    }
}