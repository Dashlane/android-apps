package com.dashlane.sync.util

import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.logD
import com.dashlane.util.logE
import com.dashlane.util.logI
import com.dashlane.util.logV
import com.dashlane.util.logW
import com.dashlane.util.stackTraceToSafeString
import java.io.File

interface SyncLogsWriter {
    val file: File?

    fun verbose(tag: String, throwable: Throwable? = null, message: String)

    fun debug(tag: String, throwable: Throwable? = null, message: String)

    fun info(tag: String, throwable: Throwable? = null, message: String)

    fun warn(tag: String, throwable: Throwable? = null, message: String)

    fun error(tag: String, throwable: Throwable? = null, message: String)
}

@Suppress("FunctionName")
fun SyncLogsWriter(userSupportFileLogger: UserSupportFileLogger): SyncLogsWriter =
    SyncLogsWriterImpl(userSupportFileLogger)

@Suppress("EXPERIMENTAL_API_USAGE")
private class SyncLogsWriterImpl(private val userSupportFileLogger: UserSupportFileLogger) : SyncLogsWriter {

    override val file: File?
        get() = userSupportFileLogger.logFile

    override fun verbose(tag: String, throwable: Throwable?, message: String) {
        
        logV(tag, throwable) { message }
    }

    override fun debug(tag: String, throwable: Throwable?, message: String) {
        log("D", tag, message, throwable)
        logD(tag, throwable) { message }
    }

    override fun info(tag: String, throwable: Throwable?, message: String) {
        log("I", tag, message, throwable)
        logI(tag, throwable) { message }
    }

    override fun warn(tag: String, throwable: Throwable?, message: String) {
        log("W", tag, message, throwable)
        logW(tag, throwable) { message }
    }

    override fun error(tag: String, throwable: Throwable?, message: String) {
        log("E", tag, message, throwable)
        logE(tag, throwable) { message }
    }

    private fun log(verbosity: String, tag: String, message: String, throwable: Throwable?) {
        var msg = "$verbosity/ [$tag] $message"
        if (throwable != null) {
            msg += "\n" + throwable.stackTraceToSafeString()
        }
        userSupportFileLogger.add(msg)
    }
}