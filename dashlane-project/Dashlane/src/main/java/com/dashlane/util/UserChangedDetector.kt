package com.dashlane.util

import com.dashlane.session.SessionManager

class UserChangedDetector(
    private val sessionManager: SessionManager
) {

    var lastUsername: String? = null
        private set

    fun hasUserChanged(): Boolean {
        val oldUsername = lastUsername
        refresh()
        return oldUsername != lastUsername
    }

    fun refresh() {
        lastUsername = sessionManager.session?.userId
    }
}
