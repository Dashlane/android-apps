package com.dashlane.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FileUtils {
    suspend fun writeFileToPublicFolder(
        context: Context,
        name: String,
        type: String,
        size: Long? = null,
        callback: suspend (OutputStream) -> Unit
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(context, name, type, size, callback)
        } else {
            writeToPublicFolder(context, name, type, callback)
        }
    }

    @SuppressLint("ExternalStorageUsage")
    @Suppress("DEPRECATION")
    private suspend fun writeToPublicFolder(
        context: Context,
        name: String,
        type: String,
        callback: suspend (OutputStream) -> Unit
    ) = withContext(Dispatchers.IO) {
        val externalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!
        externalPublicDir.mkdirs()
        val externalDownloadFile = createUniqueFile(externalPublicDir, name)
        try {
            externalDownloadFile.outputStream().use {
                callback.invoke(it)
            }
        } catch (e: Exception) {
            
            externalDownloadFile.delete()
            throw e
        }

        
        
        val downloadManager = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
        val id = downloadManager.run {
            addCompletedDownload(
                name,
                context.getString(R.string.dashlane_main_app_name),
                true,
                type,
                externalDownloadFile.path,
                externalDownloadFile.length(),
                true
            )
        }
        downloadManager.getUriForDownloadedFile(id)
    }

    private fun createUniqueFile(directory: File, name: String) =
        generateSequence(0L, Long::inc)
            .map {
                val filename = when (it) {
                    0L -> name
                    else -> "$name ($it)"
                }

                File(directory, filename)
            }
            .first { !it.exists() }

    @TargetApi(Build.VERSION_CODES.Q)
    private suspend fun writeToMediaStore(
        context: Context,
        name: String,
        type: String,
        size: Long?,
        callback: suspend (OutputStream) -> Unit
    ) = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val uri = createMediaStoreFilePending(name, type, size, resolver)
        try {
            resolver.openFileDescriptor(uri, "w", null).use { pfd ->
                
                callback.invoke(FileOutputStream(pfd!!.fileDescriptor))
            }
        } catch (e: Exception) {
            
            resolver.delete(uri, null, null)
            throw e
        }
        updateMediaStoreFileNotPending(resolver, uri)
        uri
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createMediaStoreFilePending(
        name: String,
        type: String,
        size: Long?,
        resolver: ContentResolver
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, type)
            size?.let {
                put(MediaStore.Downloads.SIZE, it)
            }
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        var uri = resolver.insert(collection, values)
        if (uri == null) {
            
            
            
            val dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ss").withLocale(Locale.ROOT)
            val dateStamp = dateFormat.format(LocalDateTime.now())
            val timestampedName = name.substringBeforeLast('.') + dateStamp + "." + name.substringAfterLast('.')
            values.put(MediaStore.Downloads.DISPLAY_NAME, timestampedName)
            uri = resolver.insert(collection, values)
        }
        if (uri == null) {
            
            throw IOException("Unable to create new file $name.")
        }
        return uri
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateMediaStoreFileNotPending(resolver: ContentResolver, uri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        resolver.update(uri, values, null, null)
    }
}

fun File.deleteFolder(): Boolean =
    listFiles()?.all { if (it.isDirectory) it.deleteFolder() else it.delete() } != false && delete()