package com.dashlane.m2w

import android.content.Context
import android.content.Intent

object M2wIntentCoordinator {
    internal const val EXTRA_M2W_COMPLETED = "m2w_completed"
    internal const val EXTRA_M2W_SKIPPED = "m2w_skipped"

    fun createConnectActivityIntent(
        context: Context
    ) = Intent(context, M2wConnectActivity::class.java)

    fun isM2wCompleted(intent: Intent) = intent.getBooleanExtra(EXTRA_M2W_COMPLETED, false)

    fun isM2wSkipped(intent: Intent) = intent.getBooleanExtra(EXTRA_M2W_SKIPPED, false)
}
