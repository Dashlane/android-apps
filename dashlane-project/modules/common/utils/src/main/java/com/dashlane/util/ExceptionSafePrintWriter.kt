package com.dashlane.util

import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer

class ExceptionSafePrintWriter(out: Writer) : PrintWriter(out) {
    override fun println(x: Any?) {
        if (x is Throwable) {
            
            super.println(x.javaClass.name)
        } else {
            
            super.println(x)
        }
    }
}

fun Throwable.stackTraceToSafeString(): String {
    val stringWriter = StringWriter()
    ExceptionSafePrintWriter(stringWriter).use {
        printStackTrace(it)
    }
    return stringWriter.toString()
}

fun Throwable.allCauseStackTraceToSafeString(): String {
    val stringWriter = StringWriter()
    ExceptionSafePrintWriter(stringWriter).use {
        printStackTrace(it)
        var currentCause = cause
        while (currentCause != null) {
            currentCause.printStackTrace(it)
            currentCause = currentCause.cause
        }
    }
    return stringWriter.toString()
}