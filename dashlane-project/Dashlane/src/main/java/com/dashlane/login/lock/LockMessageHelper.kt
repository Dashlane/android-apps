package com.dashlane.login.lock

import android.content.Context
import com.dashlane.vault.summary.SummaryObject

interface LockMessageHelper {
    fun getMessageUnlockForItem(context: Context, item: SummaryObject): String
}