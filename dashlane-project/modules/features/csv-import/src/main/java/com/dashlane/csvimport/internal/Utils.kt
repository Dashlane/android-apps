package com.dashlane.csvimport.internal

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager

internal val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)