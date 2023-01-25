package com.dashlane.attachment.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract



class OpenDocumentResultContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit) =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            
            
            addCategory(Intent.CATEGORY_OPENABLE)

            type = "*/*"
        }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.data
}