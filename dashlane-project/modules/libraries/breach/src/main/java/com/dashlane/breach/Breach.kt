package com.dashlane.breach

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.format.DateUtils
import com.dashlane.breach.utils.BreachDateSerializer
import com.dashlane.breach.utils.CriticalitySerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalSerializationApi::class)
@SuppressLint("ParcelCreator")
@Parcelize
@Serializable
@JsonIgnoreUnknownKeys
data class Breach(
    @SerialName("id")
    val id: String,
    @EncodeDefault
    @SerialName("breachModelVersion")
    val breachModelVersion: Int = 0,
    @SerialName("name")
    val name: String? = null,
    @SerialName("domains")
    val domains: List<String>? = null,
    @SerialName("eventDate")
    val eventDate: String? = null,
    @SerialName("announcedDate")
    val announcedDate: String? = null,
    @SerialName("leakedData")
    val leakedData: List<String>? = null,
    @SerialName("impactedEmails")
    val impactedEmails: List<String>? = null,
    @EncodeDefault
    @SerialName("sensitiveDomain")
    val sensitiveDomain: Boolean = false,
    @EncodeDefault
    @SerialName("criticality")
    @Serializable(with = CriticalitySerializer::class)
    val criticality: Int = DEFAULT_CRITICALITY,
    @SerialName("restrictedArea")
    val restrictedArea: List<String>? = null,
    @SerialName("relatedLinks")
    val relatedLinks: List<String>? = null,
    @SerialName("template")
    val template: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("breachCreationDate")
    @EncodeDefault
    @Serializable(with = BreachDateSerializer::class)
    val breachCreationDate: Long = EPOCH_TIMESTAMP,
    @EncodeDefault
    @SerialName("lastModificationRevision")
    val lastModificationRevision: Int = 0,
) : Parcelable {
    val title: String?
        get() = name ?: domains?.joinToString(separator = ", ")

    val eventDateParsed: Instant
        get() = BreachDateSerializer.parseDate(eventDate)
            ?: Instant.EPOCH

    fun hasLeakedData(type: String) = leakedData?.contains(type) ?: false

    fun hasPasswordLeaked() = hasLeakedData(DATA_PASSWORD)

    fun hasCreditCardLeaked() = hasLeakedData(DATA_CREDIT_CARD)

    fun hasPrivateInformationLeaked() = hasLeakedData(DATA_USERNAME) ||
        (
            (isDarkWebBreach() && !hasLeakedData(DATA_PASSWORD)) || 
                (!isDarkWebBreach() && !hasLeakedData(DATA_EMAIL))
        ) || 
        hasLeakedData(DATA_CREDIT_CARD) ||
        hasLeakedData(DATA_PHONE) ||
        hasLeakedData(DATA_ADDRESS) ||
        hasLeakedData(DATA_SSN) ||
        hasLeakedData(DATA_IP) ||
        hasLeakedData(DATA_LOCATION) ||
        hasLeakedData(DATA_PERSONAL_INFORMATION) ||
        hasLeakedData(DATA_SOCIAL_NETWORK)

    fun getDateEventFormated(context: Context): String? {
        val time = eventDateParsed
        val dashCount = eventDate?.count { it == '-' } ?: 0
        if (time <= Instant.EPOCH) return null
        return when {
            dashCount == 2 -> 
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(time.atZone(ZoneId.systemDefault()))
            dashCount == 1 -> 
                DateUtils.formatDateTime(
                    context,
                    time.toEpochMilli(),
                    DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NO_MONTH_DAY,
                )
            eventDate?.length == 4 && dashCount == 0 -> 
                time.atZone(ZoneId.systemDefault()).year.toString()
            else -> null
        }
    }

    fun shouldBeDisplay(): Boolean {
        return status == STATUS_LIVE ||
            status == STATUS_LEGACY ||
            status == STATUS_STAGING
    }

    fun isDarkWebBreach() = impactedEmails?.isNotEmpty() ?: false

    companion object {
        const val DATA_USERNAME = "username"
        const val DATA_PASSWORD = "password"
        const val DATA_EMAIL = "email"
        const val DATA_CREDIT_CARD = "creditcard"
        const val DATA_PHONE = "phone"
        const val DATA_ADDRESS = "address"
        const val DATA_SSN = "ssn"
        const val DATA_IP = "ip"
        const val DATA_LOCATION = "geolocation"
        const val DATA_PERSONAL_INFORMATION = "personalinfo"
        const val DATA_SOCIAL_NETWORK = "social"

        const val DEFAULT_CRITICALITY = 1
        const val EPOCH_TIMESTAMP = 0L

        const val STATUS_LIVE = "live"

        const val STATUS_STAGING = "staging"

        const val STATUS_LEGACY = "legacy"
    }
}