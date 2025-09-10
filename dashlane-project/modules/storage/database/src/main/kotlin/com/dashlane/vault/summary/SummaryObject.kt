package com.dashlane.vault.summary

import com.dashlane.vault.model.SyncState
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlin.math.max

@JsonClass(generateAdapter = true)
data class CommonSummary(
    @Json(name = "id") val id: String,
    @Json(name = "syncObjectType") val syncObjectType: SyncObjectType,
    @Json(name = "spaceId") val spaceId: String?,
    @Json(name = "isShared") val isShared: Boolean,
    @Json(name = "attachments") val attachments: String?,
    @Json(name = "creationDatetime") val creationDatetime: Instant?,
    @Json(name = "userModificationDatetime") val userModificationDatetime: Instant?,
    @Json(name = "locallyViewedDate") val locallyViewedDate: Instant?,
    @Json(name = "locallyUsedCount") val locallyUsedCount: Long,
    @Json(name = "sharingPermission") val sharingPermission: String?,
    @Json(name = "syncState") val syncState: SyncState?,
    @Json(name = "isFavorite") val isFavorite: Boolean,
)

sealed class SummaryObject {
    abstract val commonSummary: CommonSummary
    val id: String
        get() = commonSummary.id
    val syncObjectType: SyncObjectType
        get() = commonSummary.syncObjectType
    val spaceId: String?
        get() = commonSummary.spaceId
    val isShared: Boolean
        get() = commonSummary.isShared
    val attachments: String?
        get() = commonSummary.attachments
    val creationDatetime: Instant?
        get() = commonSummary.creationDatetime
    val userModificationDatetime: Instant?
        get() = commonSummary.userModificationDatetime
    val locallyViewedDate: Instant?
        get() = commonSummary.locallyViewedDate
    val locallyUsedCount: Long
        get() = commonSummary.locallyUsedCount
    val sharingPermission: String?
        get() = commonSummary.sharingPermission
    val syncState: SyncState?
        get() = commonSummary.syncState
    val isFavorite: Boolean
        get() = commonSummary.isFavorite

    @JsonClass(generateAdapter = true)
    data class AuthCategory(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "categoryName") val categoryName: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class SecureNoteCategory(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "categoryName") val categoryName: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Authentifiant(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "login") val login: String?,
        @Json(name = "secondaryLogin") val secondaryLogin: String?,
        @Json(name = "email") val email: String?,
        @Json(name = "useFixedUrl") val useFixedUrl: Boolean?,
        @Json(name = "userSelectedUrl") val userSelectedUrl: String?,
        @Json(name = "url") val url: String?,
        @Json(name = "title") val title: String?,
        @Json(name = "category") val category: String?,
        @Json(name = "appMetaData") val appMetaData: AppMetaData?,
        @Json(name = "modificationDatetime") val modificationDatetime: Instant?,
        @Json(name = "checked") val checked: Boolean?,
        @Json(name = "note") val note: String?,
        @Json(name = "isPasswordEmpty") val isPasswordEmpty: Boolean,
        @Json(name = "linkedServices") val linkedServices: LinkedServices?,
        @Json(name = "hasOtpUrl") val hasOtpUrl: Boolean,
        @Json(name = "lastUsed") val lastUsed: Instant?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Address(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "addressFull") val addressFull: String?,
        @Json(name = "addressName") val addressName: String?,
        @Json(name = "city") val city: String?,
        @Json(name = "country") val country: Country?,
        @Json(name = "state") val state: String?,
        @Json(name = "zipCode") val zipCode: String?,
        @Json(name = "streetName") val streetName: String?,
        @Json(name = "building") val building: String?,
        @Json(name = "floor") val floor: String?,
        @Json(name = "door") val door: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class SecureNote(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "title") val title: String?,
        @Json(name = "secured") val secured: Boolean?,
        @Json(name = "content") val content: String?,
        @Json(name = "category") val category: String?,
        @Json(name = "type") val type: SyncObject.SecureNoteType?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class BankStatement(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "bankAccountName") val bankAccountName: String?,
        @Json(name = "bankAccountBank") val bankAccountBank: String?,
        @Json(name = "bankAccountOwner") val bankAccountOwner: String?,
        @Json(name = "bankAccountCountry") val bankAccountCountry: Country?,
        @Json(name = "isBICEmpty") val isBICEmpty: Boolean,
        @Json(name = "isIBANEmpty") val isIBANEmpty: Boolean,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Company(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "jobTitle") val jobTitle: String?,
        @Json(name = "sirenNumber") val sirenNumber: String?,
        @Json(name = "siretNumber") val siretNumber: String?,
        @Json(name = "name") val name: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Collection(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "name") val name: String?,
        @Json(name = "vaultItems") val vaultItems: List<CollectionVaultItems>?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class DriverLicence(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "number") val number: String?,
        @Json(name = "linkedIdentity") val linkedIdentity: String?,
        @Json(name = "expireDate") val expireDate: LocalDate?,
        @Json(name = "fullname") val fullname: String?,
        @Json(name = "state") val state: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Email(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "email") val email: String?,
        @Json(name = "emailName") val emailName: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class FiscalStatement(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "fiscalNumber") val fiscalNumber: String?,
        @Json(name = "linkedIdentity") val linkedIdentity: String?,
        @Json(name = "fullname") val fullname: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class IdCard(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "expireDate") val expireDate: LocalDate?,
        @Json(name = "number") val number: String?,
        @Json(name = "linkedIdentity") val linkedIdentity: String?,
        @Json(name = "fullname") val fullname: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Identity(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "firstName") val firstName: String?,
        @Json(name = "lastName") val lastName: String?,
        @Json(name = "birthDate") val birthDate: LocalDate?,
        @Json(name = "fullName") val fullName: String,
        @Json(name = "middleName") val middleName: String?,
        @Json(name = "pseudo") val pseudo: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Passport(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "linkedIdentity") val linkedIdentity: String?,
        @Json(name = "fullname") val fullname: String?,
        @Json(name = "number") val number: String?,
        @Json(name = "expireDate") val expireDate: LocalDate?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class PaymentCreditCard(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "name") val name: String?,
        @Json(name = "isCardNumberEmpty") val isCardNumberEmpty: Boolean,
        @Json(name = "isSecurityCodeEmpty") val isSecurityCodeEmpty: Boolean,
        @Json(name = "expireMonth") val expireMonth: Month?,
        @Json(name = "expireYear") val expireYear: Year?,
        @Json(name = "linkedBillingAddress") val linkedBillingAddress: String?,
        @Json(name = "color") val color: SyncObject.PaymentCreditCard.Color?,
        @Json(name = "ownerName") val ownerName: String?,
        @Json(name = "bank") val bank: String?,
        @Json(name = "cardNumberObfuscate") val cardNumberObfuscate: String?,
        @Json(name = "cardNumberLastFourDigits") val cardNumberLastFourDigits: String?,
        @Json(name = "creditCardTypeName") val creditCardTypeName: String?,
        @Json(name = "country") val country: Country?,
        @Json(name = "isNoteEmpty") val isNoteEmpty: Boolean,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class PersonalWebsite(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "name") val name: String?,
        @Json(name = "website") val website: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Phone(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "phoneName") val phoneName: String?,
        @Json(name = "numberID") val number: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class SocialSecurityStatement(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "socialSecurityFullname") val socialSecurityFullname: String?,
        @Json(name = "linkedIdentity") val linkedIdentity: String?,
        @Json(name = "dateOfBirth") val dateOfBirth: LocalDate?,
        @Json(name = "isSocialSecurityNumberEmpty") val isSocialSecurityNumberEmpty: Boolean,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class SecureFileInfo(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class SecurityBreach(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "breachId") val breachId: String?,
        @Json(name = "content") val content: String?,
        @Json(name = "status") val status: Status?,
        @Json(name = "contentRevision") val contentRevision: Long?,
    ) : SummaryObject() {
        enum class Status {
            @Json(name = "VIEWED")
            VIEWED,

            @Json(name = "ACKNOWLEDGED")
            ACKNOWLEDGED,

            @Json(name = "SOLVED")
            SOLVED,

            @Json(name = "PENDING")
            PENDING,
        }
    }

    @JsonClass(generateAdapter = true)
    data class GeneratedPassword(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "authId") val authId: String?,
        @Json(name = "generatedDate") val generatedDate: Long?,
        @Json(name = "domain") val domain: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class DataChangeHistory(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "objectId") val objectId: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class LinkedServices(
        val associatedDomains: List<AssociatedDomains>,
        val associatedAndroidApps: List<AssociatedAndroidApps>,
    ) {
        @JsonClass(generateAdapter = true)
        data class AssociatedAndroidApps(
            val linkSource: AssociatedAppsSource,
            val packageName: String?,
            val name: String?,
            val sha256CertFingerprints: List<String>,
            val sha512CertFingerprints: List<String>,
        )

        @JsonClass(generateAdapter = true)
        data class AssociatedDomains(val domain: String, val source: AssociatedDomainsSource)

        enum class AssociatedAppsSource {
            @Json(name = "dashlane")
            DASHLANE,

            @Json(name = "user")
            USER,
        }

        @JsonClass(generateAdapter = false)
        enum class AssociatedDomainsSource {
            @Json(name = "manual")
            MANUAL,

            @Json(name = "remember")
            REMEMBER,
        }
    }

    @JsonClass(generateAdapter = true)
    data class AppMetaData(val androidLinkedApplications: List<AndroidLinkedApplications>?) {
        @JsonClass(generateAdapter = true)
        data class AndroidLinkedApplications(
            val packageName: String?,
            val sha256CertFingerprints: List<String>?,
            val sha512CertFingerprints: List<String>?,
        )
    }

    @JsonClass(generateAdapter = true)
    data class Passkey(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "itemName") val itemName: String?,
        @Json(name = "credentialId") val credentialId: String?,
        @Json(name = "rpId") val rpId: String?,
        @Json(name = "rpName") val rpName: String?,
        @Json(name = "userDisplayName") val userDisplayName: String?,
        @Json(name = "keyAlgorithm") val keyAlgorithm: Long?,
        @Json(name = "note") val note: String?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class Secret(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "title") val title: String?,
        @Json(name = "content") val content: String?,
        @Json(name = "secured") val secured: Boolean?,
    ) : SummaryObject()

    @JsonClass(generateAdapter = true)
    data class WiFi(
        @Json(name = "commonSummary") override val commonSummary: CommonSummary,
        @Json(name = "ssid") val ssid: String?,
        @Json(name = "name") val name: String?,
        @Json(name = "note") val note: String?,
        @Json(name = "isPassphraseEmpty") val isPassphraseEmpty: Boolean,
        @Json(name = "hidden") val hidden: Boolean?,
        @Json(name = "encryptionType") val encryptionType: EncryptionType?,
    ) : SummaryObject() {
        enum class EncryptionType {
            @Json(name = "wpa-personal")
            WPA_PERSONAL,

            @Json(name = "wpa2-personal")
            WPA2_PERSONAL,

            @Json(name = "wep")
            WEP,

            @Json(name = "wpa3-personal")
            WPA3_PERSONAL,

            @Json(name = "unsecured")
            UNSECURED,
        }
    }
}

@JsonClass(generateAdapter = true)
data class CollectionVaultItems(
    @Json(name = "type") val type: SyncObject.CollectionDataType?,
    @Json(name = "id") val id: String?,
)

val SummaryObject.mostRecentAccessTime: Instant?
    get() {
        val creationDate = this.creationDatetime?.toEpochMilli() ?: 0L
        val userModificationDate = this.userModificationDatetime?.toEpochMilli() ?: 0L
        val locallyViewedDate = this.locallyViewedDate?.toEpochMilli() ?: 0L
        val time = max(creationDate, max(userModificationDate, locallyViewedDate))
        if (time == 0L) {
            return null
        }
        return Instant.ofEpochMilli(time)
    }