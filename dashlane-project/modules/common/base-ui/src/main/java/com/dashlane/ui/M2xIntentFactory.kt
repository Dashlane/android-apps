package com.dashlane.ui

import android.content.Intent

interface M2xIntentFactory {
    fun buildM2xConnect(): Intent
}