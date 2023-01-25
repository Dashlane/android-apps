package com.dashlane.m2w



internal interface M2wConnectLogger {
    fun logLand()
    fun logBack()
    fun logDone()
    fun logExit()
    fun logConfirmPopupShow()
    fun logConfirmPopupYes()
    fun logConfirmPopupNo()
    fun logError()
}