package com.dashlane.securearchive.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class SecureArchiveFileSelectResultContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "\1/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return chooseFileIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}