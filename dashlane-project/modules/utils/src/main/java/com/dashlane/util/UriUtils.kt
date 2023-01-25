package com.dashlane.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File



object UriUtils {

    @JvmStatic
    fun getOpenFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file)
    }

    @JvmStatic
    fun getFileDisplayName(context: Context, uri: Uri): String? = when (uri.scheme) {
        "content" -> tryOrNull {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                cursor.takeIf { it.moveToFirst() }?.getString(0)
            }
        }
        "file" -> uri.lastPathSegment
        else -> null
    }
}