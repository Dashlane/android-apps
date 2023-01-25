package com.dashlane.core.sharing

interface SharingItemUpdaterRequestCallback {
    fun onRequestSucceed()

    fun onRequestError(error: Throwable)
}
