package com.dashlane.notification

import com.dashlane.util.tryOrNull

enum class FcmCode(val code: String) {
    TOKEN("101"),
    SYNC("102"),
    DARK_WEB_SETUP_COMPLETE("103"),
    DARK_WEB_ALERT("109"),
    PUBLIC_BREACH_ALERT("108");

    companion object Provider {

        fun get(string: String): FcmCode? {
            return tryOrNull { values().firstOrNull { it.code == string } }
        }
    }
}
