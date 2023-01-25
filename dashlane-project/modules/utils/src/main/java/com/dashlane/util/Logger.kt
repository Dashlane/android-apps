
@file:SuppressWarnings("AndroidLoggerUsage")

package com.dashlane.util

import android.util.Log

@JvmOverloads
fun logV(tag: String, message: String, throwable: Throwable? = null) =
    log(Log.VERBOSE, tag, { message }, throwable, LoggingDelegate::v)

@JvmOverloads
fun logD(tag: String, message: String, throwable: Throwable? = null) =
    log(Log.VERBOSE, tag, { message }, throwable, LoggingDelegate::d)

@JvmOverloads
fun logI(tag: String, message: String, throwable: Throwable? = null) =
    log(Log.VERBOSE, tag, { message }, throwable, LoggingDelegate::i)

@JvmOverloads
fun logW(tag: String, message: String, throwable: Throwable? = null) =
    log(Log.VERBOSE, tag, { message }, throwable, LoggingDelegate::w)

@JvmOverloads
fun logE(tag: String, message: String, throwable: Throwable? = null) =
    log(Log.VERBOSE, tag, { message }, throwable, LoggingDelegate::e)

inline fun <reified T> T.logV(tag: String = getLogTag(), throwable: Throwable? = null, lazyMessage: () -> String) =
    log(Log.VERBOSE, tag.toTag(), lazyMessage, throwable, LoggingDelegate::v)

inline fun <reified T> T.logD(tag: String = getLogTag(), throwable: Throwable? = null, lazyMessage: () -> String) =
    log(Log.DEBUG, tag.toTag(), lazyMessage, throwable, LoggingDelegate::d)

inline fun <reified T> T.logI(tag: String = getLogTag(), throwable: Throwable? = null, lazyMessage: () -> String) =
    log(Log.INFO, tag.toTag(), lazyMessage, throwable, LoggingDelegate::i)

inline fun <reified T> T.logW(tag: String = getLogTag(), throwable: Throwable? = null, lazyMessage: () -> String) =
    log(Log.WARN, tag.toTag(), lazyMessage, throwable, LoggingDelegate::w)

inline fun <reified T> T.logE(tag: String = getLogTag(), throwable: Throwable? = null, lazyMessage: () -> String) =
    log(Log.ERROR, tag.toTag(), lazyMessage, throwable, LoggingDelegate::e)



@PublishedApi
internal fun String.toTag(): String =
    if (length > 23) take(23) else this



@Suppress("unused") 
inline fun <reified T> T.getLogTag(): String =
    T::class.java.simpleName

@PublishedApi
internal inline fun log(
    level: Int,
    tag: String,
    lazyMessage: () -> String,
    throwable: Throwable?,
    lazyLog: LoggingDelegate.(String, String, Throwable?) -> Unit
) {
    if (loggingDelegate.isLoggable(tag, level)) {
        lazyLog(loggingDelegate, tag, lazyMessage(), throwable)
    }
}

@PublishedApi
internal var loggingDelegate: LoggingDelegate = AndroidLoggingDelegate()

@PublishedApi
internal interface LoggingDelegate {
    fun v(tag: String, msg: String, tr: Throwable?)
    fun d(tag: String, msg: String, tr: Throwable?)
    fun i(tag: String, msg: String, tr: Throwable?)
    fun w(tag: String, msg: String, tr: Throwable?)
    fun e(tag: String, msg: String, tr: Throwable?)
    fun isLoggable(tag: String, level: Int): Boolean
}

internal class AndroidLoggingDelegate : LoggingDelegate {

    override fun v(tag: String, msg: String, tr: Throwable?) {
        Log.v(tag, msg, tr)
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        Log.d(tag, msg, tr)
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        Log.i(tag, msg, tr)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        Log.w(tag, msg, tr)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        Log.e(tag, msg, tr)
    }

    override fun isLoggable(tag: String, level: Int): Boolean =
        Log.isLoggable(tag, level)
}