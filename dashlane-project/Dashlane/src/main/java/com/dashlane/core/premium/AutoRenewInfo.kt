package com.dashlane.core.premium

import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class AutoRenewInfo constructor(
    @SerializedName("periodicity")
    private val periodicity: String? = null,
    @SerializedName("trigger")
    private val trigger: String? = null
) {

    @Periodicity
    val formattedPeriodicity: String
        get() = when (periodicity) {
            YEARLY, MONTHLY, OTHER -> periodicity
            else -> UNKNOWN
        }

    @TriggerType
    val formattedTrigger: String?
        get() = when (trigger) {
            MANUAL, AUTOMATIC -> trigger
            else -> null
        }

    constructor(jsonObject: JSONObject?) : this(
        periodicity = jsonObject?.optString(JSON_KEY_PERIODICITY),
        trigger = jsonObject?.optString(JSON_KEY_TRIGGER)
    )

    

    @StringDef(
        UNKNOWN,
        MONTHLY,
        OTHER,
        YEARLY
    )
    annotation class Periodicity

    

    @StringDef(
        MANUAL,
        AUTOMATIC
    )
    annotation class TriggerType

    companion object {
        private const val JSON_KEY_PERIODICITY = "periodicity"

        const val YEARLY = "yearly"
        const val MONTHLY = "monthly"
        const val OTHER = "other"
        const val UNKNOWN = ""

        private const val JSON_KEY_TRIGGER = "trigger"

        const val MANUAL = "manual"
        const val AUTOMATIC = "automatic"
    }
}