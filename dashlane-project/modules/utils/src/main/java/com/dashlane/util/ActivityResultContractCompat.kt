package com.dashlane.util

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract



abstract class ActivityResultContractCompat<T> : ActivityResultContract<T, Pair<Int, Intent?>>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, Intent?> =
        resultCode to intent
}
