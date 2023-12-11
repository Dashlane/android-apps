package com.dashlane.csvimport.utils

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager

internal val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)