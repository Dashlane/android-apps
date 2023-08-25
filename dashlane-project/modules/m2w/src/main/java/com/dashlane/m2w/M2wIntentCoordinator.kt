package com.dashlane.m2w

import android.content.Context
import android.content.Intent

object M2wIntentCoordinator {
    internal const val EXTRA_ORIGIN = "origin"
    internal const val EXTRA_M2W_COMPLETED = "m2w_completed"

    @JvmStatic
    fun putIntroActivityExtras(intent: Intent, origin: String) =
        intent.apply {
            putExtra(EXTRA_ORIGIN, origin)
        }

    fun createConnectActivityIntent(
        context: Context,
        origin: String
    ) = putConnectActivityExtras(
        Intent(context, M2wConnectActivity::class.java),
        origin
    )

    fun isM2wCompleted(intent: Intent) = intent.getBooleanExtra(EXTRA_M2W_COMPLETED, false)

    internal fun putConnectActivityExtras(
        intent: Intent,
        origin: String
    ) = intent.apply {
        putExtra(EXTRA_ORIGIN, origin)
    }

    fun getOrigin(intent: Intent) =
        checkNotNull(intent.getStringExtra(EXTRA_ORIGIN)) { "Origin not specified" }
}
