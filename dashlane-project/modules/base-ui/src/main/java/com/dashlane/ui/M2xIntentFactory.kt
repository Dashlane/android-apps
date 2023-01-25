package com.dashlane.ui

import android.content.Intent



interface M2xIntentFactory {
    

    fun buildM2xIntro(origin: String): Intent?

    

    fun buildM2xConnect(origin: String): Intent
}