package com.dashlane.util.log

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AndroidIdUtil
import com.dashlane.hermes.generated.definitions.Android
import com.dashlane.logger.Log
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AttributionLogData(
    val isMarketingOptIn: Boolean,
    val androidAttribution: Android?
)

class AttributionsLogDataProvider @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher
) {
    suspend fun getAttributionLogData() = withContext(defaultCoroutineDispatcher) {
        val advertisingInfo =
            runCatching { AdvertisingIdClient.getAdvertisingIdInfo(context) }
                .onFailure { Log.d("ATTRIBUTION", "Unable to get attribution info.", it) }
                .getOrNull()
        AttributionLogData(
            advertisingInfo.isMarketingOptIn,
            getAttributionTracking(advertisingInfo)
        )
    }

    private val AdvertisingIdClient.Info?.isMarketingOptIn
        get() = this != null && !isLimitAdTrackingEnabled

    private fun getAttributionTracking(advertisingInfo: AdvertisingIdClient.Info?) = Android(
        adid = Adjust.getAdid(),
        advertisingId = advertisingInfo?.id,
        androidId = AndroidIdUtil.getAndroidId(context)
    )
}