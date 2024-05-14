package com.dashlane.breach

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.format.DateUtils
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoField

@SuppressLint("ParcelCreator")
@Parcelize
data class Breach(
    @SerializedName("id")
    val id: String,
    @SerializedName("breachModelVersion")
    val breachModelVersion: Int = 0,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("domains")
    val domains: List<String>? = null,
    @SerializedName("eventDate")
    val eventDate: String? = null,
    @SerializedName("announcedDate")
    val announcedDate: String? = null,
    @SerializedName("leakedData")
    val leakedData: List<String>? = null,
    @SerializedName("impactedEmails")
    val impactedEmails: List<String>? = null,
    @SerializedName("sensitiveDomain")
    val sensitiveDomain: Boolean = false,
    @SerializedName("criticality")
    val criticality: Int = 0,
    @SerializedName("restrictedArea")
    val restrictedArea: List<String>? = null,
    @SerializedName("relatedLinks")
    val relatedLinks: List<String>? = null,
    @SerializedName("template")
    val template: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("breachCreationDate")
    val breachCreationDate: Long = 0,
    @SerializedName("lastModificationRevision")
    val lastModificationRevision: Int = 0
) : Parcelable {

    val title: String?
        get() = name ?: domains?.joinToString(separator = ", ")

    val eventDateParsed: Instant
        get() = parseDate(eventDate) ?: Instant.EPOCH

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

    private fun parseDate(date: String?): Instant? {
        date ?: return null
        
        date.toLongOrNull()
            ?.takeIf { it > 10_000 } 
            ?.let {
                return if (it < 100_000_000_000L) {
                    Instant.ofEpochSecond(it) 
                } else {
                    Instant.ofEpochMilli(it) 
                }
            }
        
        DATES_FORMAT.forEach { (pattern, timeToAdd) ->
            val formatter = DateTimeFormatter.ofPattern(pattern)
            val accessor = try {
                formatter.parse(date)
            } catch (e: Exception) {
                
                return@forEach
            }
            val localDate = LocalDate.of(
                accessor.get(ChronoField.YEAR),
                if (accessor.isSupported(ChronoField.MONTH_OF_YEAR)) accessor.get(ChronoField.MONTH_OF_YEAR) else 1,
                if (accessor.isSupported(ChronoField.DAY_OF_MONTH)) accessor.get(ChronoField.DAY_OF_MONTH) else 1
            )
            return ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.systemDefault()).toInstant() + timeToAdd
        }
        return null
    }

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
                    DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NO_MONTH_DAY
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

        const val STATUS_LIVE = "live"

        const val STATUS_STAGING = "staging"

        const val STATUS_LEGACY = "legacy"

        private val DATES_FORMAT = listOf(
            "yyyy-MM-dd" to Duration.ZERO, 
            "yyyy-MM" to Duration.ofDays(30), 
            "yyyy" to Duration.ofDays(364) 
        )
    }
}