package com.dashlane.securefile.extensions

import android.content.ContentResolver
import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.net.Uri
import android.provider.OpenableColumns
import com.dashlane.util.tryOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun ContentResolver.getFileName(uri: Uri): String? = withContext(Dispatchers.IO) {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val index = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = c.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result!!.substring(cut + 1)
        }
    }
    result
}

suspend fun ContentResolver.getFileSize(uri: Uri): Long = withContext(Dispatchers.IO) {
    if (uri.scheme == SCHEME_CONTENT) {
        
        val cursor = query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val index = c.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) {
                    val result = c.getLong(index)
                    if (result > 0L) return@withContext result
                }
            }
        }
        
        tryOrNull { openInputStream(uri).use { it?.available()?.toLong() } }?.let { return@withContext it }
    } else if (uri.scheme == SCHEME_FILE) {
        tryOrNull { File(uri.path ?: "").length() }?.let { return@withContext it }
    }
    0L
}

suspend fun ContentResolver.getFileType(uri: Uri): String = withContext(Dispatchers.IO) {
    getType(uri) ?: "application/octet-stream"
}
