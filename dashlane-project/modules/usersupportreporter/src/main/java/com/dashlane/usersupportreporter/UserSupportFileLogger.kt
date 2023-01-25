package com.dashlane.usersupportreporter

import android.content.Context
import android.os.Build
import com.dashlane.util.PackageUtilities.getAppVersionName
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class UserSupportFileLogger @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {

    val timestamp: String
        get() = dateFormat.format(ZonedDateTime.now(ZoneOffset.UTC))

    val logFile: File?
        get() = File(context.filesDir, LOG_FILE_NAME).also {
            runCatching { it.exists() || it.createNewFile() }.getOrDefault(false)
        }

    
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val saveLogActor = globalCoroutineScope.actor<String>(
        context = ioCoroutineDispatcher,
        capacity = Channel.UNLIMITED
    ) {
        val versionName = context.getAppVersionName()

        for (msg in this) {
            val timestamp = timestamp
            val msgs = listOf(msg) + pollAll()

            val file = logFile
            if (file == null || !file.canWrite()) {
                
                continue
            }

            trimFileIfNecessary(file)

            FileOutputStream(file, true).bufferedWriter().use { writer ->
                msgs.forEach { writer.appendLog(timestamp, Build.VERSION.SDK_INT, versionName, it) }
            }
        }
    }

    

    fun add(msg: String) {
        saveLogActor.trySend(msg)
    }

    

    private fun trimFileIfNecessary(file: File) {
        val fileSize = file.length()
        if (fileSize < maxFileSize) {
            
            return
        }
        val raf = RandomAccessFile(file, "rw")
        
        var writePosition = raf.filePointer
        while (raf.filePointer < fileSize / 2) {
            raf.readLine()
        }
        
        var readPosition = raf.filePointer

        val buff = ByteArray(1024)
        do {
            val n = raf.read(buff)
            if (n == -1) {
                break
            }
            raf.seek(writePosition)
            raf.write(buff, 0, n)
            readPosition += n.toLong()
            writePosition += n.toLong()
            raf.seek(readPosition)
        } while (true)
        raf.setLength(writePosition)
        raf.close()
    }

    companion object {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US)
        private const val LOG_FILE_NAME = "usersupport.log"
        private const val maxFileSize = 2 * 1024 * 1024 

        private fun <E : Any> ReceiveChannel<E>.pollAll(): List<E> {
            val results = mutableListOf<E>()

            var value = tryReceive().getOrNull()

            while (value != null) {
                results += value
                value = tryReceive().getOrNull()
            }

            return results
        }

        private fun BufferedWriter.appendLog(
            timestamp: String,
            sdkInt: Int,
            versionName: String,
            msg: String
        ) = append(timestamp)
            .append(" OS:").append(sdkInt.toString())
            .append("/DL:").append(versionName)
            .append(" :: ").append(msg)
            .appendLine()
    }
}