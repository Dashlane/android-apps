package com.dashlane.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.use
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.Writer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class FileUtils @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun writeFileToPublicFolder(
        name: String,
        type: String,
        size: Long? = null,
        callback: suspend (OutputStream) -> Unit
    ): Uri {
        return writeToMediaStore(context, name, type, size, callback)
    }

    suspend fun writeFileToCacheFolder(
        fileName: String,
        child: String,
        callback: suspend (Writer) -> Unit
    ): File {
        val dir = File(context.cacheDir, child)
        dir.mkdirs()

        val newFile = File(dir, fileName)
        withContext(ioDispatcher) {
            newFile.createNewFile()
            callback(newFile.writer())
        }

        return newFile
    }

    private suspend fun writeToMediaStore(
        context: Context,
        name: String,
        type: String,
        size: Long?,
        callback: suspend (OutputStream) -> Unit
    ) = withContext(ioDispatcher) {
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
            
            
            
            val dateFormat =
                DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ss").withLocale(Locale.ROOT)
            val dateStamp = dateFormat.format(LocalDateTime.now())
            val timestampedName =
                name.substringBeforeLast('.') + dateStamp + "." + name.substringAfterLast('.')
            values.put(MediaStore.Downloads.DISPLAY_NAME, timestampedName)
            uri = resolver.insert(collection, values)
        }
        if (uri == null) {
            
            throw IOException("Unable to create new file $name.")
        }
        return uri
    }

    private fun updateMediaStoreFileNotPending(resolver: ContentResolver, uri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        resolver.update(uri, values, null, null)
    }
}

fun File.deleteFolder(): Boolean =
    listFiles()?.all { if (it.isDirectory) it.deleteFolder() else it.delete() } != false && delete()