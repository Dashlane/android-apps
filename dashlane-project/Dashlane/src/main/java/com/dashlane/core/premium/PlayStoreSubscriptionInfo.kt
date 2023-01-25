package com.dashlane.core.premium

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class PlayStoreSubscriptionInfo(
    @SerializedName("purchasetoken")val purchaseToken: String
) {

    companion object {
        @JvmStatic
        fun from(status: JSONObject, key: String): PlayStoreSubscriptionInfo? {
            val jsonObject = status.optJSONObject(key) ?: return null
            return Gson().fromJson<PlayStoreSubscriptionInfo>(
                jsonObject.toString(),
                PlayStoreSubscriptionInfo::class.java
            )
        }
    }
}