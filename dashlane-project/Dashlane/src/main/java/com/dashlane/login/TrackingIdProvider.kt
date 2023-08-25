package com.dashlane.login

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.dashlane.login.TrackingIdProvider.TRACKING_ID
import java.util.UUID

object TrackingIdProvider {

    const val TRACKING_ID = "trackingSessionId"

    fun getOrGenerateTrackingId(activity: FragmentActivity): String {
        val savedStateRegistry = activity.savedStateRegistry
        val restoredState = savedStateRegistry.consumeRestoredStateForKey(TRACKING_ID)
        val trackingId = if (restoredState == null) {
            activity.intent.getStringExtra(TRACKING_ID) ?: UUID.randomUUID().toString()
        } else {
            checkNotNull(restoredState.getString(TRACKING_ID))
        }
        savedStateRegistry.registerSavedStateProvider(TRACKING_ID) {
            Bundle().apply { putString(TRACKING_ID, trackingId) }
        }
        return trackingId
    }
}