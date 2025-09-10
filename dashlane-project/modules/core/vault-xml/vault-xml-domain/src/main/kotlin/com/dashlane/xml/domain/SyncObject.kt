package com.dashlane.xml.domain

import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.XmlData
import com.dashlane.xml.asCollectionValueOrNull
import com.dashlane.xml.asEnumValue
import com.dashlane.xml.asItemNodeObfuscatedValueOrNull
import com.dashlane.xml.asItemValueOrNull
import com.dashlane.xml.asJsonDataClassOrNull
import com.dashlane.xml.asJsonDataListClassOrNull
import com.dashlane.xml.asListValueOrNull
import com.dashlane.xml.domain.properties.AddressXml
import com.dashlane.xml.domain.properties.AuthCategoryXml
import com.dashlane.xml.domain.properties.AuthentifiantXml
import com.dashlane.xml.domain.properties.BankStatementXml
import com.dashlane.xml.domain.properties.CollectionXml
import com.dashlane.xml.domain.properties.CompanyXml
import com.dashlane.xml.domain.properties.DataChangeHistoryXml
import com.dashlane.xml.domain.properties.DriverLicenceXml
import com.dashlane.xml.domain.properties.EmailXml
import com.dashlane.xml.domain.properties.FiscalStatementXml
import com.dashlane.xml.domain.properties.GeneratedPasswordXml
import com.dashlane.xml.domain.properties.IdCardXml
import com.dashlane.xml.domain.properties.IdentityXml
import com.dashlane.xml.domain.properties.PasskeyXml
import com.dashlane.xml.domain.properties.PassportXml
import com.dashlane.xml.domain.properties.PaymentCreditCardXml
import com.dashlane.xml.domain.properties.PersonalWebsiteXml
import com.dashlane.xml.domain.properties.PhoneXml
import com.dashlane.xml.domain.properties.SecretXml
import com.dashlane.xml.domain.properties.SecureFileInfoXml
import com.dashlane.xml.domain.properties.SecureNoteCategoryXml
import com.dashlane.xml.domain.properties.SecureNoteXml
import com.dashlane.xml.domain.properties.SecurityBreachXml
import com.dashlane.xml.domain.properties.SettingsXml
import com.dashlane.xml.domain.properties.SocialSecurityStatementXml
import com.dashlane.xml.domain.properties.SyncObjectXml
import com.dashlane.xml.domain.properties.WiFiXml
import com.dashlane.xml.domain.time.toInstantOrNull
import com.dashlane.xml.domain.time.toLocalDateOrNull
import com.dashlane.xml.domain.time.toMonthValueOrNull
import com.dashlane.xml.domain.time.toXmlString
import com.dashlane.xml.domain.time.toYearValueOrNull
import com.dashlane.xml.domain.utils.Country
import com.dashlane.xml.domain.utils.decodeBase64
import com.dashlane.xml.domain.utils.encodeBase64
import com.dashlane.xml.domain.utils.mapValuesNotNull
import com.dashlane.xml.domain.utils.set
import com.dashlane.xml.domain.utils.toBooleanValue
import com.dashlane.xml.domain.utils.toCountryOrNull
import com.dashlane.xml.domain.utils.toLongValueOrNull
import com.dashlane.xml.toCollectionNode
import com.dashlane.xml.toItemListNodeFromJson
import com.dashlane.xml.toItemNode
import com.dashlane.xml.toItemNodeFromEnum
import com.dashlane.xml.toItemNodeFromJson
import com.dashlane.xml.toItemNodeObfuscated
import com.dashlane.xml.toListNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.Year
import javax.annotation.processing.Generated

@Suppress(
    "LargeClass",
    "ktlint:standard:max-line-length",
)
@Generated
sealed class SyncObject {
    abstract val data: Map<String, XmlData>
    abstract val userContentData: Map<String, XmlData>?

    val creationDatetime: Instant?
        get() = data[SyncObjectXml.CREATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()

    val localeFormat: Country?
        get() = data[SyncObjectXml.LOCALE_FORMAT]?.asItemValueOrNull()?.toCountryOrNull()

    val spaceId: String?
        get() = data[SyncObjectXml.SPACE_ID]?.asItemValueOrNull()

    val userModificationDatetime: Instant?
        get() = data[SyncObjectXml.USER_MODIFICATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()

    val id: String?
        get() = data[SyncObjectXml.ID]?.asItemValueOrNull()

    val attachments: String?
        get() = data[SyncObjectXml.ATTACHMENTS]?.asItemValueOrNull()

    val isFavorite: Boolean?
        get() = data[SyncObjectXml.IS_FAVORITE]?.asItemValueOrNull()?.toBooleanValue()

    private val builderData: MutableMap<String, XmlData>
        get() = data.toMutableMap()

    abstract fun builder(data: MutableMap<String, XmlData> = builderData): Builder

    @Generated
    data class Address(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.address }.filterValues { !it.isEmpty() }

        val building: String?
            get() = data[AddressXml.BUILDING]?.asItemValueOrNull()

        val digitCode: String?
            get() = data[AddressXml.DIGIT_CODE]?.asItemValueOrNull()

        val streetName: String?
            get() = data[AddressXml.STREET_NAME]?.asItemValueOrNull()

        val floor: String?
            get() = data[AddressXml.FLOOR]?.asItemValueOrNull()

        val streetTitle: String?
            get() = data[AddressXml.STREET_TITLE]?.asItemValueOrNull()

        val zipCode: String?
            get() = data[AddressXml.ZIP_CODE]?.asItemValueOrNull()

        val addressFull: String?
            get() = data[AddressXml.ADDRESS_FULL]?.asItemValueOrNull()

        val linkedPhone: String?
            get() = data[AddressXml.LINKED_PHONE]?.asItemValueOrNull()

        val receiver: String?
            get() = data[AddressXml.RECEIVER]?.asItemValueOrNull()

        val city: String?
            get() = data[AddressXml.CITY]?.asItemValueOrNull()

        val stateNumber: String?
            get() = data[AddressXml.STATE_NUMBER]?.asItemValueOrNull()

        val door: String?
            get() = data[AddressXml.DOOR]?.asItemValueOrNull()

        val addressName: String?
            get() = data[AddressXml.ADDRESS_NAME]?.asItemValueOrNull()

        val stateLevel2: String?
            get() = data[AddressXml.STATE_LEVEL2]?.asItemValueOrNull()

        val state: String?
            get() = data[AddressXml.STATE]?.asItemValueOrNull()

        val country: Country?
            get() = data[AddressXml.COUNTRY]?.asItemValueOrNull()?.toCountryOrNull()

        val streetNumber: String?
            get() = data[AddressXml.STREET_NUMBER]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Address =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Address =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var building: String?
                get() = data[AddressXml.BUILDING]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.BUILDING] = value?.toItemNode()
                }

            var digitCode: String?
                get() = data[AddressXml.DIGIT_CODE]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.DIGIT_CODE] = value?.toItemNode()
                }

            var streetName: String?
                get() = data[AddressXml.STREET_NAME]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STREET_NAME] = value?.toItemNode()
                }

            var floor: String?
                get() = data[AddressXml.FLOOR]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.FLOOR] = value?.toItemNode()
                }

            var streetTitle: String?
                get() = data[AddressXml.STREET_TITLE]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STREET_TITLE] = value?.toItemNode()
                }

            var zipCode: String?
                get() = data[AddressXml.ZIP_CODE]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.ZIP_CODE] = value?.toItemNode()
                }

            var addressFull: String?
                get() = data[AddressXml.ADDRESS_FULL]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.ADDRESS_FULL] = value?.toItemNode()
                }

            var linkedPhone: String?
                get() = data[AddressXml.LINKED_PHONE]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.LINKED_PHONE] = value?.toItemNode()
                }

            var receiver: String?
                get() = data[AddressXml.RECEIVER]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.RECEIVER] = value?.toItemNode()
                }

            var city: String?
                get() = data[AddressXml.CITY]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.CITY] = value?.toItemNode()
                }

            var stateNumber: String?
                get() = data[AddressXml.STATE_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STATE_NUMBER] = value?.toItemNode()
                }

            var door: String?
                get() = data[AddressXml.DOOR]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.DOOR] = value?.toItemNode()
                }

            var addressName: String?
                get() = data[AddressXml.ADDRESS_NAME]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.ADDRESS_NAME] = value?.toItemNode()
                }

            var stateLevel2: String?
                get() = data[AddressXml.STATE_LEVEL2]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STATE_LEVEL2] = value?.toItemNode()
                }

            var state: String?
                get() = data[AddressXml.STATE]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STATE] = value?.toItemNode()
                }

            var country: Country?
                get() = data[AddressXml.COUNTRY]?.asItemValueOrNull()?.toCountryOrNull()
                set(value) {
                    data[AddressXml.COUNTRY] = value?.isoCode?.toItemNode()
                }

            var streetNumber: String?
                get() = data[AddressXml.STREET_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[AddressXml.STREET_NUMBER] = value?.toItemNode()
                }

            override fun build(): Address =
                Address(data.toMap())
        }
    }

    @Generated
    data class AuthCategory(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.authCategory }.filterValues { !it.isEmpty() }

        val categoryName: String?
            get() = data[AuthCategoryXml.CATEGORY_NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): AuthCategory =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): AuthCategory =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var categoryName: String?
                get() = data[AuthCategoryXml.CATEGORY_NAME]?.asItemValueOrNull()
                set(value) {
                    data[AuthCategoryXml.CATEGORY_NAME] = value?.toItemNode()
                }

            override fun build(): AuthCategory =
                AuthCategory(data.toMap())
        }
    }

    @Generated
    data class Authentifiant(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.authentifiant }.filterValues { !it.isEmpty() }

        val otpUrl: SyncObfuscatedValue?
            get() = data[AuthentifiantXml.OTP_URL]?.asItemNodeObfuscatedValueOrNull()

        val category: String?
            get() = data[AuthentifiantXml.CATEGORY]?.asItemValueOrNull()

        val email: String?
            get() = data[AuthentifiantXml.EMAIL]?.asItemValueOrNull()

        val secondaryLogin: String?
            get() = data[AuthentifiantXml.SECONDARY_LOGIN]?.asItemValueOrNull()

        val autoProtected: Boolean?
            get() = data[AuthentifiantXml.AUTO_PROTECTED]?.asItemValueOrNull()?.toBooleanValue()

        val url: String?
            get() = data[AuthentifiantXml.URL]?.asItemValueOrNull()

        val useFixedUrl: Boolean?
            get() = data[AuthentifiantXml.USE_FIXED_URL]?.asItemValueOrNull()?.toBooleanValue()

        val numberUse: Long?
            get() = data[AuthentifiantXml.NUMBER_USE]?.asItemValueOrNull()?.toLongValueOrNull()

        val modificationDatetime: Instant?
            get() = data[AuthentifiantXml.MODIFICATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()

        val lastUse: Instant?
            get() = data[AuthentifiantXml.LAST_USE]?.asItemValueOrNull()?.toInstantOrNull()

        val otpSecret: SyncObfuscatedValue?
            get() = data[AuthentifiantXml.OTP_SECRET]?.asItemNodeObfuscatedValueOrNull()

        val password: SyncObfuscatedValue?
            get() = data[AuthentifiantXml.PASSWORD]?.asItemNodeObfuscatedValueOrNull()

        val subdomainOnly: Boolean?
            get() = data[AuthentifiantXml.SUBDOMAIN_ONLY]?.asItemValueOrNull()?.toBooleanValue()

        val title: String?
            get() = data[AuthentifiantXml.TITLE]?.asItemValueOrNull()

        val login: String?
            get() = data[AuthentifiantXml.LOGIN]?.asItemValueOrNull()

        val appMetaData: AppMetaData?
            get() = data[AuthentifiantXml.APP_META_DATA]?.asJsonDataClassOrNull()

        val appMetaDataLastUpdate: String?
            get() = data[AuthentifiantXml.APP_META_DATA_LAST_UPDATE]?.asItemValueOrNull()

        val checked: Boolean?
            get() = data[AuthentifiantXml.CHECKED]?.asItemValueOrNull()?.toBooleanValue()

        val customFields: List<CustomFields>?
            get() = data[AuthentifiantXml.CUSTOM_FIELDS]?.asJsonDataListClassOrNull()

        val linkedServices: LinkedServices?
            get() = data[AuthentifiantXml.LINKED_SERVICES]?.asJsonDataClassOrNull()

        val autoLogin: Boolean?
            get() = data[AuthentifiantXml.AUTO_LOGIN]?.asItemValueOrNull()?.toBooleanValue()

        val note: String?
            get() = data[AuthentifiantXml.NOTE]?.asItemValueOrNull()

        val trustedUrlGroup: List<TrustedUrlGroup>?
            get() = data[AuthentifiantXml.TRUSTED_URL_GROUP]?.asListValueOrNull()?.mapNotNull { it.asTrustedUrlGroupOrNull() }

        val strength: Long?
            get() = data[AuthentifiantXml.STRENGTH]?.asItemValueOrNull()?.toLongValueOrNull()

        val userSelectedUrl: String?
            get() = data[AuthentifiantXml.USER_SELECTED_URL]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Authentifiant =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Authentifiant =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var otpUrl: SyncObfuscatedValue?
                get() = data[AuthentifiantXml.OTP_URL]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[AuthentifiantXml.OTP_URL] = value?.toItemNodeObfuscated()
                }

            var category: String?
                get() = data[AuthentifiantXml.CATEGORY]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.CATEGORY] = value?.toItemNode()
                }

            var email: String?
                get() = data[AuthentifiantXml.EMAIL]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.EMAIL] = value?.toItemNode()
                }

            var secondaryLogin: String?
                get() = data[AuthentifiantXml.SECONDARY_LOGIN]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.SECONDARY_LOGIN] = value?.toItemNode()
                }

            var autoProtected: Boolean?
                get() = data[AuthentifiantXml.AUTO_PROTECTED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[AuthentifiantXml.AUTO_PROTECTED] = value?.toString()?.toItemNode()
                }

            var url: String?
                get() = data[AuthentifiantXml.URL]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.URL] = value?.toItemNode()
                }

            var useFixedUrl: Boolean?
                get() = data[AuthentifiantXml.USE_FIXED_URL]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[AuthentifiantXml.USE_FIXED_URL] = value?.toString()?.toItemNode()
                }

            var numberUse: Long?
                get() = data[AuthentifiantXml.NUMBER_USE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[AuthentifiantXml.NUMBER_USE] = value?.toString()?.toItemNode()
                }

            var modificationDatetime: Instant?
                get() = data[AuthentifiantXml.MODIFICATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()
                set(value) {
                    data[AuthentifiantXml.MODIFICATION_DATETIME] = value?.toXmlString()?.toItemNode()
                }

            var lastUse: Instant?
                get() = data[AuthentifiantXml.LAST_USE]?.asItemValueOrNull()?.toInstantOrNull()
                set(value) {
                    data[AuthentifiantXml.LAST_USE] = value?.toXmlString()?.toItemNode()
                }

            var otpSecret: SyncObfuscatedValue?
                get() = data[AuthentifiantXml.OTP_SECRET]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[AuthentifiantXml.OTP_SECRET] = value?.toItemNodeObfuscated()
                }

            var password: SyncObfuscatedValue?
                get() = data[AuthentifiantXml.PASSWORD]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[AuthentifiantXml.PASSWORD] = value?.toItemNodeObfuscated()
                }

            var subdomainOnly: Boolean?
                get() = data[AuthentifiantXml.SUBDOMAIN_ONLY]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[AuthentifiantXml.SUBDOMAIN_ONLY] = value?.toString()?.toItemNode()
                }

            var title: String?
                get() = data[AuthentifiantXml.TITLE]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.TITLE] = value?.toItemNode()
                }

            var login: String?
                get() = data[AuthentifiantXml.LOGIN]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.LOGIN] = value?.toItemNode()
                }

            var appMetaData: AppMetaData?
                get() = data[AuthentifiantXml.APP_META_DATA]?.asJsonDataClassOrNull()
                set(value) {
                    data[AuthentifiantXml.APP_META_DATA] = value?.toItemNodeFromJson()
                }

            var appMetaDataLastUpdate: String?
                get() = data[AuthentifiantXml.APP_META_DATA_LAST_UPDATE]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.APP_META_DATA_LAST_UPDATE] = value?.toItemNode()
                }

            var checked: Boolean?
                get() = data[AuthentifiantXml.CHECKED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[AuthentifiantXml.CHECKED] = value?.toString()?.toItemNode()
                }

            var customFields: List<CustomFields>?
                get() = data[AuthentifiantXml.CUSTOM_FIELDS]?.asJsonDataListClassOrNull()
                set(value) {
                    data[AuthentifiantXml.CUSTOM_FIELDS] = value?.toItemListNodeFromJson()
                }

            var linkedServices: LinkedServices?
                get() = data[AuthentifiantXml.LINKED_SERVICES]?.asJsonDataClassOrNull()
                set(value) {
                    data[AuthentifiantXml.LINKED_SERVICES] = value?.toItemNodeFromJson()
                }

            var autoLogin: Boolean?
                get() = data[AuthentifiantXml.AUTO_LOGIN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[AuthentifiantXml.AUTO_LOGIN] = value?.toString()?.toItemNode()
                }

            var note: String?
                get() = data[AuthentifiantXml.NOTE]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.NOTE] = value?.toItemNode()
                }

            var trustedUrlGroup: List<TrustedUrlGroup>?
                get() = data[AuthentifiantXml.TRUSTED_URL_GROUP]?.asListValueOrNull()?.mapNotNull { it.asTrustedUrlGroupOrNull() }
                set(value) {
                    data[AuthentifiantXml.TRUSTED_URL_GROUP] = value?.map { it.toCollectionNode() }?.toListNode()
                }

            var strength: Long?
                get() = data[AuthentifiantXml.STRENGTH]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[AuthentifiantXml.STRENGTH] = value?.toString()?.toItemNode()
                }

            var userSelectedUrl: String?
                get() = data[AuthentifiantXml.USER_SELECTED_URL]?.asItemValueOrNull()
                set(value) {
                    data[AuthentifiantXml.USER_SELECTED_URL] = value?.toItemNode()
                }

            override fun build(): Authentifiant =
                Authentifiant(data.toMap())
        }

        @Generated
        data class TrustedUrlGroup(val data: Map<String, XmlData>) {
            val trustedUrl: String?
                get() = data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL]?.asItemValueOrNull()

            val trustedUrlExpire: Long?
                get() = data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL_EXPIRE]?.asItemValueOrNull()?.toLongValueOrNull()

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): TrustedUrlGroup =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): TrustedUrlGroup =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                var trustedUrl: String?
                    get() = data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL]?.asItemValueOrNull()
                    set(value) {
                        data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL] = value?.toItemNode()
                    }

                var trustedUrlExpire: Long?
                    get() = data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL_EXPIRE]?.asItemValueOrNull()?.toLongValueOrNull()
                    set(value) {
                        data[AuthentifiantXml.TrustedUrlGroupXml.TRUSTED_URL_EXPIRE] = value?.toString()?.toItemNode()
                    }

                fun build(): TrustedUrlGroup =
                    TrustedUrlGroup(data.toMap())
            }
        }

        @Generated
        @JsonClass(generateAdapter = true)
        data class AppMetaData(
            @Json(name = "android_linked_applications")
            val androidLinkedApplications: List<AndroidLinkedApplications>?,
        ) {
            @Generated
            @JsonClass(generateAdapter = true)
            data class AndroidLinkedApplications(
                @Json(name = "package_name")
                val packageName: String?,
                @Json(name = "sha256_cert_fingerprints")
                val sha256CertFingerprints: List<String>?,
                @Json(name = "sha512_cert_fingerprints")
                val sha512CertFingerprints: List<String>?,
            )
        }

        @Generated
        @JsonClass(generateAdapter = true)
        data class CustomFields(
            @Json(name = "protected")
            val protected: Boolean?,
            @Json(name = "label")
            val label: String?,
            @Json(name = "content")
            val content: String?,
        )

        @Generated
        @JsonClass(generateAdapter = true)
        data class LinkedServices(
            @Json(name = "associated_android_apps")
            val associatedAndroidApps: List<AssociatedAndroidApps>?,
            @Json(name = "associated_domains")
            val associatedDomains: List<AssociatedDomains>?,
        ) {
            @Generated
            @JsonClass(generateAdapter = true)
            data class AssociatedAndroidApps(
                @Json(name = "link_source")
                val linkSource: LinkSource?,
                @Json(name = "name")
                val name: String?,
                @Json(name = "package_name")
                val packageName: String?,
                @Json(name = "sha256_cert_fingerprints")
                val sha256CertFingerprints: List<String>?,
                @Json(name = "sha512_cert_fingerprints")
                val sha512CertFingerprints: List<String>?,
            ) {
                enum class LinkSource(override val value: String) : SyncObjectEnum {
                    @Json(name = "dashlane")
                    DASHLANE("dashlane"),

                    @Json(name = "user")
                    USER("user"),
                }
            }

            @Generated
            @JsonClass(generateAdapter = true)
            data class AssociatedDomains(
                @Json(name = "domain")
                val domain: String?,
                @Json(name = "source")
                val source: Source?,
            ) {
                enum class Source(override val value: String) : SyncObjectEnum {
                    @Json(name = "remember")
                    REMEMBER("remember"),

                    @Json(name = "manual")
                    MANUAL("manual"),
                }
            }
        }
    }

    @Generated
    data class BankStatement(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.bankStatement }.filterValues { !it.isEmpty() }

        val bankAccountName: String?
            get() = data[BankStatementXml.BANK_ACCOUNT_NAME]?.asItemValueOrNull()

        val bankAccountBank: String?
            get() = data[BankStatementXml.BANK_ACCOUNT_BANK]?.asItemValueOrNull()

        val bankAccountBIC: SyncObfuscatedValue?
            get() = data[BankStatementXml.BANK_ACCOUNT_B_I_C]?.asItemNodeObfuscatedValueOrNull()

        val bankAccountOwner: String?
            get() = data[BankStatementXml.BANK_ACCOUNT_OWNER]?.asItemValueOrNull()

        val bankAccountIBAN: SyncObfuscatedValue?
            get() = data[BankStatementXml.BANK_ACCOUNT_I_B_A_N]?.asItemNodeObfuscatedValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): BankStatement =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): BankStatement =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var bankAccountName: String?
                get() = data[BankStatementXml.BANK_ACCOUNT_NAME]?.asItemValueOrNull()
                set(value) {
                    data[BankStatementXml.BANK_ACCOUNT_NAME] = value?.toItemNode()
                }

            var bankAccountBank: String?
                get() = data[BankStatementXml.BANK_ACCOUNT_BANK]?.asItemValueOrNull()
                set(value) {
                    data[BankStatementXml.BANK_ACCOUNT_BANK] = value?.toItemNode()
                }

            var bankAccountBIC: SyncObfuscatedValue?
                get() = data[BankStatementXml.BANK_ACCOUNT_B_I_C]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[BankStatementXml.BANK_ACCOUNT_B_I_C] = value?.toItemNodeObfuscated()
                }

            var bankAccountOwner: String?
                get() = data[BankStatementXml.BANK_ACCOUNT_OWNER]?.asItemValueOrNull()
                set(value) {
                    data[BankStatementXml.BANK_ACCOUNT_OWNER] = value?.toItemNode()
                }

            var bankAccountIBAN: SyncObfuscatedValue?
                get() = data[BankStatementXml.BANK_ACCOUNT_I_B_A_N]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[BankStatementXml.BANK_ACCOUNT_I_B_A_N] = value?.toItemNodeObfuscated()
                }

            override fun build(): BankStatement =
                BankStatement(data.toMap())
        }
    }

    @Generated
    data class Collection(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val name: String?
            get() = data[CollectionXml.NAME]?.asItemValueOrNull()

        val vaultItems: List<VaultItems>?
            get() = data[CollectionXml.VAULT_ITEMS]?.asListValueOrNull()?.mapNotNull { it.asVaultItemsOrNull() }

        inline fun copy(builder: Builder.() -> Unit = {}): Collection =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Collection =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var name: String?
                get() = data[CollectionXml.NAME]?.asItemValueOrNull()
                set(value) {
                    data[CollectionXml.NAME] = value?.toItemNode()
                }

            var vaultItems: List<VaultItems>?
                get() = data[CollectionXml.VAULT_ITEMS]?.asListValueOrNull()?.mapNotNull { it.asVaultItemsOrNull() }
                set(value) {
                    data[CollectionXml.VAULT_ITEMS] = value?.map { it.toCollectionNode() }?.toListNode()
                }

            override fun build(): Collection =
                Collection(data.toMap())
        }

        @Generated
        data class VaultItems(val data: Map<String, XmlData>) {
            val id: String?
                get() = data[CollectionXml.VaultItemsXml.ID]?.asItemValueOrNull()

            val type: CollectionDataType?
                get() = data[CollectionXml.VaultItemsXml.TYPE]?.asEnumValue()

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): VaultItems =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): VaultItems =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                var id: String?
                    get() = data[CollectionXml.VaultItemsXml.ID]?.asItemValueOrNull()
                    set(value) {
                        data[CollectionXml.VaultItemsXml.ID] = value?.toItemNode()
                    }

                var type: CollectionDataType?
                    get() = data[CollectionXml.VaultItemsXml.TYPE]?.asEnumValue()
                    set(value) {
                        data[CollectionXml.VaultItemsXml.TYPE] = value?.toItemNodeFromEnum()
                    }

                fun build(): VaultItems =
                    VaultItems(data.toMap())
            }
        }
    }

    @Generated
    data class Company(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.company }.filterValues { !it.isEmpty() }

        val tvaNumber: String?
            get() = data[CompanyXml.TVA_NUMBER]?.asItemValueOrNull()

        val siretNumber: String?
            get() = data[CompanyXml.SIRET_NUMBER]?.asItemValueOrNull()

        val nafCode: String?
            get() = data[CompanyXml.NAF_CODE]?.asItemValueOrNull()

        val jobTitle: String?
            get() = data[CompanyXml.JOB_TITLE]?.asItemValueOrNull()

        val sirenNumber: String?
            get() = data[CompanyXml.SIREN_NUMBER]?.asItemValueOrNull()

        val name: String?
            get() = data[CompanyXml.NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Company =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Company =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var tvaNumber: String?
                get() = data[CompanyXml.TVA_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.TVA_NUMBER] = value?.toItemNode()
                }

            var siretNumber: String?
                get() = data[CompanyXml.SIRET_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.SIRET_NUMBER] = value?.toItemNode()
                }

            var nafCode: String?
                get() = data[CompanyXml.NAF_CODE]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.NAF_CODE] = value?.toItemNode()
                }

            var jobTitle: String?
                get() = data[CompanyXml.JOB_TITLE]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.JOB_TITLE] = value?.toItemNode()
                }

            var sirenNumber: String?
                get() = data[CompanyXml.SIREN_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.SIREN_NUMBER] = value?.toItemNode()
                }

            var name: String?
                get() = data[CompanyXml.NAME]?.asItemValueOrNull()
                set(value) {
                    data[CompanyXml.NAME] = value?.toItemNode()
                }

            override fun build(): Company =
                Company(data.toMap())
        }
    }

    @Generated
    data class DataChangeHistory(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val objectTitle: String?
            get() = data[DataChangeHistoryXml.OBJECT_TITLE]?.asItemValueOrNull()

        val objectType: String?
            get() = data[DataChangeHistoryXml.OBJECT_TYPE]?.asItemValueOrNull()

        val objectId: String?
            get() = data[DataChangeHistoryXml.OBJECT_ID]?.asItemValueOrNull()

        val changeSets: List<ChangeSet>?
            get() = data[DataChangeHistoryXml.CHANGE_SETS]?.asListValueOrNull()?.mapNotNull { it.asChangeSetOrNull() }

        inline fun copy(builder: Builder.() -> Unit = {}): DataChangeHistory =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): DataChangeHistory =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var objectTitle: String?
                get() = data[DataChangeHistoryXml.OBJECT_TITLE]?.asItemValueOrNull()
                set(value) {
                    data[DataChangeHistoryXml.OBJECT_TITLE] = value?.toItemNode()
                }

            var objectType: String?
                get() = data[DataChangeHistoryXml.OBJECT_TYPE]?.asItemValueOrNull()
                set(value) {
                    data[DataChangeHistoryXml.OBJECT_TYPE] = value?.toItemNode()
                }

            var objectId: String?
                get() = data[DataChangeHistoryXml.OBJECT_ID]?.asItemValueOrNull()
                set(value) {
                    data[DataChangeHistoryXml.OBJECT_ID] = value?.toItemNode()
                }

            var changeSets: List<ChangeSet>?
                get() = data[DataChangeHistoryXml.CHANGE_SETS]?.asListValueOrNull()?.mapNotNull { it.asChangeSetOrNull() }
                set(value) {
                    data[DataChangeHistoryXml.CHANGE_SETS] = value?.map { it.toObjectNode() }?.toListNode()
                }

            override fun build(): DataChangeHistory =
                DataChangeHistory(data.toMap())
        }

        @Generated
        data class ChangeSet(val data: Map<String, XmlData>) {
            val changedProperties: List<String>?
                get() = data[DataChangeHistoryXml.ChangeSetXml.CHANGED_PROPERTIES]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

            val currentData: Map<String, String>?
                get() = data[DataChangeHistoryXml.ChangeSetXml.CURRENT_DATA]?.asCollectionValueOrNull()?.mapValuesNotNull {
                        (_, value) ->
                    value.asItemValueOrNull()
                }

            val deviceName: String?
                get() = data[DataChangeHistoryXml.ChangeSetXml.DEVICE_NAME]?.asItemValueOrNull()

            val id: String?
                get() = data[DataChangeHistoryXml.ChangeSetXml.ID]?.asItemValueOrNull()

            val modificationDate: Instant?
                get() = data[DataChangeHistoryXml.ChangeSetXml.MODIFICATION_DATE]?.asItemValueOrNull()?.toInstantOrNull()

            val platform: Platform?
                get() = data[DataChangeHistoryXml.ChangeSetXml.PLATFORM]?.asEnumValue()

            val removed: Boolean?
                get() = data[DataChangeHistoryXml.ChangeSetXml.REMOVED]?.asItemValueOrNull()?.toBooleanValue()

            val user: String?
                get() = data[DataChangeHistoryXml.ChangeSetXml.USER]?.asItemValueOrNull()

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): ChangeSet =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): ChangeSet =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                var changedProperties: List<String>?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.CHANGED_PROPERTIES]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.CHANGED_PROPERTIES] = value?.map { it.toItemNode() }?.toListNode()
                    }

                var currentData: Map<String, String>?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.CURRENT_DATA]?.asCollectionValueOrNull()?.mapValuesNotNull {
                            (_, value) ->
                        value.asItemValueOrNull()
                    }
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.CURRENT_DATA] = value?.mapValues {
                                (_, it) ->
                            it.toItemNode()
                        }?.toCollectionNode()
                    }

                var deviceName: String?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.DEVICE_NAME]?.asItemValueOrNull()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.DEVICE_NAME] = value?.toItemNode()
                    }

                var id: String?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.ID]?.asItemValueOrNull()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.ID] = value?.toItemNode()
                    }

                var modificationDate: Instant?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.MODIFICATION_DATE]?.asItemValueOrNull()?.toInstantOrNull()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.MODIFICATION_DATE] = value?.toXmlString()?.toItemNode()
                    }

                var platform: Platform?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.PLATFORM]?.asEnumValue()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.PLATFORM] = value?.toItemNodeFromEnum()
                    }

                var removed: Boolean?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.REMOVED]?.asItemValueOrNull()?.toBooleanValue()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.REMOVED] = value?.toString()?.toItemNode()
                    }

                var user: String?
                    get() = data[DataChangeHistoryXml.ChangeSetXml.USER]?.asItemValueOrNull()
                    set(value) {
                        data[DataChangeHistoryXml.ChangeSetXml.USER] = value?.toItemNode()
                    }

                fun build(): ChangeSet =
                    ChangeSet(data.toMap())
            }
        }
    }

    @Generated
    data class DriverLicence(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.driverLicence }.filterValues { !it.isEmpty() }

        val dateOfBirth: LocalDate?
            get() = data[DriverLicenceXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()

        val expireDate: LocalDate?
            get() = data[DriverLicenceXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val number: String?
            get() = data[DriverLicenceXml.NUMBER]?.asItemValueOrNull()

        val fullname: String?
            get() = data[DriverLicenceXml.FULLNAME]?.asItemValueOrNull()

        val state: String?
            get() = data[DriverLicenceXml.STATE]?.asItemValueOrNull()

        val sex: Gender?
            get() = data[DriverLicenceXml.SEX]?.asEnumValue()

        val deliveryDate: LocalDate?
            get() = data[DriverLicenceXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val linkedIdentity: String?
            get() = data[DriverLicenceXml.LINKED_IDENTITY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): DriverLicence =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): DriverLicence =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var dateOfBirth: LocalDate?
                get() = data[DriverLicenceXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[DriverLicenceXml.DATE_OF_BIRTH] = value?.toXmlString()?.toItemNode()
                }

            var expireDate: LocalDate?
                get() = data[DriverLicenceXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[DriverLicenceXml.EXPIRE_DATE] = value?.toXmlString()?.toItemNode()
                }

            var number: String?
                get() = data[DriverLicenceXml.NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[DriverLicenceXml.NUMBER] = value?.toItemNode()
                }

            var fullname: String?
                get() = data[DriverLicenceXml.FULLNAME]?.asItemValueOrNull()
                set(value) {
                    data[DriverLicenceXml.FULLNAME] = value?.toItemNode()
                }

            var state: String?
                get() = data[DriverLicenceXml.STATE]?.asItemValueOrNull()
                set(value) {
                    data[DriverLicenceXml.STATE] = value?.toItemNode()
                }

            var sex: Gender?
                get() = data[DriverLicenceXml.SEX]?.asEnumValue()
                set(value) {
                    data[DriverLicenceXml.SEX] = value?.toItemNodeFromEnum()
                }

            var deliveryDate: LocalDate?
                get() = data[DriverLicenceXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[DriverLicenceXml.DELIVERY_DATE] = value?.toXmlString()?.toItemNode()
                }

            var linkedIdentity: String?
                get() = data[DriverLicenceXml.LINKED_IDENTITY]?.asItemValueOrNull()
                set(value) {
                    data[DriverLicenceXml.LINKED_IDENTITY] = value?.toItemNode()
                }

            override fun build(): DriverLicence =
                DriverLicence(data.toMap())
        }
    }

    @Generated
    data class Email(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val type: Type?
            get() = data[EmailXml.TYPE]?.asEnumValue()

        val email: String?
            get() = data[EmailXml.EMAIL]?.asItemValueOrNull()

        val emailName: String?
            get() = data[EmailXml.EMAIL_NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Email =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Email =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var type: Type?
                get() = data[EmailXml.TYPE]?.asEnumValue()
                set(value) {
                    data[EmailXml.TYPE] = value?.toItemNodeFromEnum()
                }

            var email: String?
                get() = data[EmailXml.EMAIL]?.asItemValueOrNull()
                set(value) {
                    data[EmailXml.EMAIL] = value?.toItemNode()
                }

            var emailName: String?
                get() = data[EmailXml.EMAIL_NAME]?.asItemValueOrNull()
                set(value) {
                    data[EmailXml.EMAIL_NAME] = value?.toItemNode()
                }

            override fun build(): Email =
                Email(data.toMap())
        }

        enum class Type(override val value: String) : SyncObjectEnum {
            @Json(name = "PRO")
            PRO("PRO"),

            @Json(name = "PERSO")
            PERSO("PERSO"),

            @Json(name = "NO_TYPE")
            NO_TYPE("NO_TYPE"),
        }
    }

    @Generated
    data class FiscalStatement(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.fiscalStatement }.filterValues { !it.isEmpty() }

        val teledeclarantNumber: String?
            get() = data[FiscalStatementXml.TELEDECLARANT_NUMBER]?.asItemValueOrNull()

        val fiscalNumber: String?
            get() = data[FiscalStatementXml.FISCAL_NUMBER]?.asItemValueOrNull()

        val fullname: String?
            get() = data[FiscalStatementXml.FULLNAME]?.asItemValueOrNull()

        val linkedIdentity: String?
            get() = data[FiscalStatementXml.LINKED_IDENTITY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): FiscalStatement =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): FiscalStatement =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var teledeclarantNumber: String?
                get() = data[FiscalStatementXml.TELEDECLARANT_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[FiscalStatementXml.TELEDECLARANT_NUMBER] = value?.toItemNode()
                }

            var fiscalNumber: String?
                get() = data[FiscalStatementXml.FISCAL_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[FiscalStatementXml.FISCAL_NUMBER] = value?.toItemNode()
                }

            var fullname: String?
                get() = data[FiscalStatementXml.FULLNAME]?.asItemValueOrNull()
                set(value) {
                    data[FiscalStatementXml.FULLNAME] = value?.toItemNode()
                }

            var linkedIdentity: String?
                get() = data[FiscalStatementXml.LINKED_IDENTITY]?.asItemValueOrNull()
                set(value) {
                    data[FiscalStatementXml.LINKED_IDENTITY] = value?.toItemNode()
                }

            override fun build(): FiscalStatement =
                FiscalStatement(data.toMap())
        }
    }

    @Generated
    data class GeneratedPassword(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.generatedPassword }.filterValues { !it.isEmpty() }

        val authId: String?
            get() = data[GeneratedPasswordXml.AUTH_ID]?.asItemValueOrNull()

        val platform: Platform?
            get() = data[GeneratedPasswordXml.PLATFORM]?.asEnumValue()

        val generatedDate: Long?
            get() = data[GeneratedPasswordXml.GENERATED_DATE]?.asItemValueOrNull()?.toLongValueOrNull()

        val domain: String?
            get() = data[GeneratedPasswordXml.DOMAIN]?.asItemValueOrNull()

        val password: SyncObfuscatedValue?
            get() = data[GeneratedPasswordXml.PASSWORD]?.asItemNodeObfuscatedValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): GeneratedPassword =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): GeneratedPassword =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var authId: String?
                get() = data[GeneratedPasswordXml.AUTH_ID]?.asItemValueOrNull()
                set(value) {
                    data[GeneratedPasswordXml.AUTH_ID] = value?.toItemNode()
                }

            var platform: Platform?
                get() = data[GeneratedPasswordXml.PLATFORM]?.asEnumValue()
                set(value) {
                    data[GeneratedPasswordXml.PLATFORM] = value?.toItemNodeFromEnum()
                }

            var generatedDate: Long?
                get() = data[GeneratedPasswordXml.GENERATED_DATE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[GeneratedPasswordXml.GENERATED_DATE] = value?.toString()?.toItemNode()
                }

            var domain: String?
                get() = data[GeneratedPasswordXml.DOMAIN]?.asItemValueOrNull()
                set(value) {
                    data[GeneratedPasswordXml.DOMAIN] = value?.toItemNode()
                }

            var password: SyncObfuscatedValue?
                get() = data[GeneratedPasswordXml.PASSWORD]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[GeneratedPasswordXml.PASSWORD] = value?.toItemNodeObfuscated()
                }

            override fun build(): GeneratedPassword =
                GeneratedPassword(data.toMap())
        }
    }

    @Generated
    data class IdCard(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.idCard }.filterValues { !it.isEmpty() }

        val dateOfBirth: LocalDate?
            get() = data[IdCardXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()

        val expireDate: LocalDate?
            get() = data[IdCardXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val number: String?
            get() = data[IdCardXml.NUMBER]?.asItemValueOrNull()

        val fullname: String?
            get() = data[IdCardXml.FULLNAME]?.asItemValueOrNull()

        val sex: Gender?
            get() = data[IdCardXml.SEX]?.asEnumValue()

        val deliveryDate: LocalDate?
            get() = data[IdCardXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val linkedIdentity: String?
            get() = data[IdCardXml.LINKED_IDENTITY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): IdCard =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): IdCard =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var dateOfBirth: LocalDate?
                get() = data[IdCardXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[IdCardXml.DATE_OF_BIRTH] = value?.toXmlString()?.toItemNode()
                }

            var expireDate: LocalDate?
                get() = data[IdCardXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[IdCardXml.EXPIRE_DATE] = value?.toXmlString()?.toItemNode()
                }

            var number: String?
                get() = data[IdCardXml.NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[IdCardXml.NUMBER] = value?.toItemNode()
                }

            var fullname: String?
                get() = data[IdCardXml.FULLNAME]?.asItemValueOrNull()
                set(value) {
                    data[IdCardXml.FULLNAME] = value?.toItemNode()
                }

            var sex: Gender?
                get() = data[IdCardXml.SEX]?.asEnumValue()
                set(value) {
                    data[IdCardXml.SEX] = value?.toItemNodeFromEnum()
                }

            var deliveryDate: LocalDate?
                get() = data[IdCardXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[IdCardXml.DELIVERY_DATE] = value?.toXmlString()?.toItemNode()
                }

            var linkedIdentity: String?
                get() = data[IdCardXml.LINKED_IDENTITY]?.asItemValueOrNull()
                set(value) {
                    data[IdCardXml.LINKED_IDENTITY] = value?.toItemNode()
                }

            override fun build(): IdCard =
                IdCard(data.toMap())
        }
    }

    @Generated
    data class Identity(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val type: String?
            get() = data[IdentityXml.TYPE]?.asItemValueOrNull()

        val birthPlace: String?
            get() = data[IdentityXml.BIRTH_PLACE]?.asItemValueOrNull()

        val firstName: String?
            get() = data[IdentityXml.FIRST_NAME]?.asItemValueOrNull()

        val title: Title?
            get() = data[IdentityXml.TITLE]?.asEnumValue()

        val pseudo: String?
            get() = data[IdentityXml.PSEUDO]?.asItemValueOrNull()

        val lastName2: String?
            get() = data[IdentityXml.LAST_NAME2]?.asItemValueOrNull()

        val lastName: String?
            get() = data[IdentityXml.LAST_NAME]?.asItemValueOrNull()

        val middleName: String?
            get() = data[IdentityXml.MIDDLE_NAME]?.asItemValueOrNull()

        val birthDate: LocalDate?
            get() = data[IdentityXml.BIRTH_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Identity =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Identity =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var type: String?
                get() = data[IdentityXml.TYPE]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.TYPE] = value?.toItemNode()
                }

            var birthPlace: String?
                get() = data[IdentityXml.BIRTH_PLACE]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.BIRTH_PLACE] = value?.toItemNode()
                }

            var firstName: String?
                get() = data[IdentityXml.FIRST_NAME]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.FIRST_NAME] = value?.toItemNode()
                }

            var title: Title?
                get() = data[IdentityXml.TITLE]?.asEnumValue()
                set(value) {
                    data[IdentityXml.TITLE] = value?.toItemNodeFromEnum()
                }

            var pseudo: String?
                get() = data[IdentityXml.PSEUDO]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.PSEUDO] = value?.toItemNode()
                }

            var lastName2: String?
                get() = data[IdentityXml.LAST_NAME2]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.LAST_NAME2] = value?.toItemNode()
                }

            var lastName: String?
                get() = data[IdentityXml.LAST_NAME]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.LAST_NAME] = value?.toItemNode()
                }

            var middleName: String?
                get() = data[IdentityXml.MIDDLE_NAME]?.asItemValueOrNull()
                set(value) {
                    data[IdentityXml.MIDDLE_NAME] = value?.toItemNode()
                }

            var birthDate: LocalDate?
                get() = data[IdentityXml.BIRTH_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[IdentityXml.BIRTH_DATE] = value?.toXmlString()?.toItemNode()
                }

            override fun build(): Identity =
                Identity(data.toMap())
        }

        enum class Title(override val value: String) : SyncObjectEnum {
            @Json(name = "MME")
            MME("MME"),

            @Json(name = "MR")
            MR("MR"),

            @Json(name = "MS")
            MS("MS"),

            @Json(name = "MX")
            MX("MX"),

            @Json(name = "MLLE")
            MLLE("MLLE"),

            @Json(name = "NONE_OF_THESE")
            NONE_OF_THESE("NONE_OF_THESE"),
        }
    }

    @Generated
    data class Passkey(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val counter: Long?
            get() = data[PasskeyXml.COUNTER]?.asItemValueOrNull()?.toLongValueOrNull()

        val privateKey: PrivateKey?
            get() = data[PasskeyXml.PRIVATE_KEY]?.asPrivateKeyOrNull()

        val credentialId: String?
            get() = data[PasskeyXml.CREDENTIAL_ID]?.asItemValueOrNull()

        val rpName: String?
            get() = data[PasskeyXml.RP_NAME]?.asItemValueOrNull()

        val userHandle: String?
            get() = data[PasskeyXml.USER_HANDLE]?.asItemValueOrNull()

        val note: String?
            get() = data[PasskeyXml.NOTE]?.asItemValueOrNull()

        val cloudPasskey: CloudPasskey?
            get() = data[PasskeyXml.CLOUD_PASSKEY]?.asCloudPasskeyOrNull()

        val itemName: String?
            get() = data[PasskeyXml.ITEM_NAME]?.asItemValueOrNull()

        val userDisplayName: String?
            get() = data[PasskeyXml.USER_DISPLAY_NAME]?.asItemValueOrNull()

        val rpId: String?
            get() = data[PasskeyXml.RP_ID]?.asItemValueOrNull()

        val keyAlgorithm: Long?
            get() = data[PasskeyXml.KEY_ALGORITHM]?.asItemValueOrNull()?.toLongValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Passkey =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Passkey =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var counter: Long?
                get() = data[PasskeyXml.COUNTER]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[PasskeyXml.COUNTER] = value?.toString()?.toItemNode()
                }

            var privateKey: PrivateKey?
                get() = data[PasskeyXml.PRIVATE_KEY]?.asPrivateKeyOrNull()
                set(value) {
                    data[PasskeyXml.PRIVATE_KEY] = value?.toCollectionNode()
                }

            var credentialId: String?
                get() = data[PasskeyXml.CREDENTIAL_ID]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.CREDENTIAL_ID] = value?.toItemNode()
                }

            var rpName: String?
                get() = data[PasskeyXml.RP_NAME]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.RP_NAME] = value?.toItemNode()
                }

            var userHandle: String?
                get() = data[PasskeyXml.USER_HANDLE]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.USER_HANDLE] = value?.toItemNode()
                }

            var note: String?
                get() = data[PasskeyXml.NOTE]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.NOTE] = value?.toItemNode()
                }

            var cloudPasskey: CloudPasskey?
                get() = data[PasskeyXml.CLOUD_PASSKEY]?.asCloudPasskeyOrNull()
                set(value) {
                    data[PasskeyXml.CLOUD_PASSKEY] = value?.toCollectionNode()
                }

            var itemName: String?
                get() = data[PasskeyXml.ITEM_NAME]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.ITEM_NAME] = value?.toItemNode()
                }

            var userDisplayName: String?
                get() = data[PasskeyXml.USER_DISPLAY_NAME]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.USER_DISPLAY_NAME] = value?.toItemNode()
                }

            var rpId: String?
                get() = data[PasskeyXml.RP_ID]?.asItemValueOrNull()
                set(value) {
                    data[PasskeyXml.RP_ID] = value?.toItemNode()
                }

            var keyAlgorithm: Long?
                get() = data[PasskeyXml.KEY_ALGORITHM]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[PasskeyXml.KEY_ALGORITHM] = value?.toString()?.toItemNode()
                }

            override fun build(): Passkey =
                Passkey(data.toMap())
        }

        @Generated
        data class PrivateKey(val data: Map<String, XmlData>) {
            val crv: Crv?
                get() = data[PasskeyXml.PrivateKeyXml.CRV]?.asEnumValue()

            val d: String?
                get() = data[PasskeyXml.PrivateKeyXml.D]?.asItemValueOrNull()

            val ext: Boolean?
                get() = data[PasskeyXml.PrivateKeyXml.EXT]?.asItemValueOrNull()?.toBooleanValue()

            val keyOps: List<String>?
                get() = data[PasskeyXml.PrivateKeyXml.KEY_OPS]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

            val kty: Kty?
                get() = data[PasskeyXml.PrivateKeyXml.KTY]?.asEnumValue()

            val x: String?
                get() = data[PasskeyXml.PrivateKeyXml.X]?.asItemValueOrNull()

            val y: String?
                get() = data[PasskeyXml.PrivateKeyXml.Y]?.asItemValueOrNull()

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): PrivateKey =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): PrivateKey =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                var crv: Crv?
                    get() = data[PasskeyXml.PrivateKeyXml.CRV]?.asEnumValue()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.CRV] = value?.toItemNodeFromEnum()
                    }

                var d: String?
                    get() = data[PasskeyXml.PrivateKeyXml.D]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.D] = value?.toItemNode()
                    }

                var ext: Boolean?
                    get() = data[PasskeyXml.PrivateKeyXml.EXT]?.asItemValueOrNull()?.toBooleanValue()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.EXT] = value?.toString()?.toItemNode()
                    }

                var keyOps: List<String>?
                    get() = data[PasskeyXml.PrivateKeyXml.KEY_OPS]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.KEY_OPS] = value?.map { it.toItemNode() }?.toListNode()
                    }

                var kty: Kty?
                    get() = data[PasskeyXml.PrivateKeyXml.KTY]?.asEnumValue()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.KTY] = value?.toItemNodeFromEnum()
                    }

                var x: String?
                    get() = data[PasskeyXml.PrivateKeyXml.X]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.X] = value?.toItemNode()
                    }

                var y: String?
                    get() = data[PasskeyXml.PrivateKeyXml.Y]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.PrivateKeyXml.Y] = value?.toItemNode()
                    }

                fun build(): PrivateKey =
                    PrivateKey(data.toMap())
            }
        }

        enum class Crv(override val value: String) : SyncObjectEnum {
            P_256("P-256"),
        }

        enum class Kty(override val value: String) : SyncObjectEnum {
            EC("EC"),
        }

        @Generated
        data class CloudPasskey(val data: Map<String, XmlData>) {
            val passkeyEncryptionKey: String?
                get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY]?.asItemValueOrNull()

            val passkeyEncryptionKeyId: String?
                get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY_ID]?.asItemValueOrNull()

            val passkeyId: String?
                get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ID]?.asItemValueOrNull()

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): CloudPasskey =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): CloudPasskey =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                var passkeyEncryptionKey: String?
                    get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY] = value?.toItemNode()
                    }

                var passkeyEncryptionKeyId: String?
                    get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY_ID]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.CloudPasskeyXml.PASSKEY_ENCRYPTION_KEY_ID] = value?.toItemNode()
                    }

                var passkeyId: String?
                    get() = data[PasskeyXml.CloudPasskeyXml.PASSKEY_ID]?.asItemValueOrNull()
                    set(value) {
                        data[PasskeyXml.CloudPasskeyXml.PASSKEY_ID] = value?.toItemNode()
                    }

                fun build(): CloudPasskey =
                    CloudPasskey(data.toMap())
            }
        }
    }

    @Generated
    data class Passport(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.passport }.filterValues { !it.isEmpty() }

        val dateOfBirth: LocalDate?
            get() = data[PassportXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()

        val number: String?
            get() = data[PassportXml.NUMBER]?.asItemValueOrNull()

        val expireDate: LocalDate?
            get() = data[PassportXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val fullname: String?
            get() = data[PassportXml.FULLNAME]?.asItemValueOrNull()

        val sex: Gender?
            get() = data[PassportXml.SEX]?.asEnumValue()

        val deliveryPlace: String?
            get() = data[PassportXml.DELIVERY_PLACE]?.asItemValueOrNull()

        val deliveryDate: LocalDate?
            get() = data[PassportXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()

        val linkedIdentity: String?
            get() = data[PassportXml.LINKED_IDENTITY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Passport =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Passport =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var dateOfBirth: LocalDate?
                get() = data[PassportXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[PassportXml.DATE_OF_BIRTH] = value?.toXmlString()?.toItemNode()
                }

            var number: String?
                get() = data[PassportXml.NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[PassportXml.NUMBER] = value?.toItemNode()
                }

            var expireDate: LocalDate?
                get() = data[PassportXml.EXPIRE_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[PassportXml.EXPIRE_DATE] = value?.toXmlString()?.toItemNode()
                }

            var fullname: String?
                get() = data[PassportXml.FULLNAME]?.asItemValueOrNull()
                set(value) {
                    data[PassportXml.FULLNAME] = value?.toItemNode()
                }

            var sex: Gender?
                get() = data[PassportXml.SEX]?.asEnumValue()
                set(value) {
                    data[PassportXml.SEX] = value?.toItemNodeFromEnum()
                }

            var deliveryPlace: String?
                get() = data[PassportXml.DELIVERY_PLACE]?.asItemValueOrNull()
                set(value) {
                    data[PassportXml.DELIVERY_PLACE] = value?.toItemNode()
                }

            var deliveryDate: LocalDate?
                get() = data[PassportXml.DELIVERY_DATE]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[PassportXml.DELIVERY_DATE] = value?.toXmlString()?.toItemNode()
                }

            var linkedIdentity: String?
                get() = data[PassportXml.LINKED_IDENTITY]?.asItemValueOrNull()
                set(value) {
                    data[PassportXml.LINKED_IDENTITY] = value?.toItemNode()
                }

            override fun build(): Passport =
                Passport(data.toMap())
        }
    }

    @Generated
    data class PaymentCreditCard(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.paymentCreditCard }.filterValues { !it.isEmpty() }

        val cardNumber: SyncObfuscatedValue?
            get() = data[PaymentCreditCardXml.CARD_NUMBER]?.asItemNodeObfuscatedValueOrNull()

        val ownerName: String?
            get() = data[PaymentCreditCardXml.OWNER_NAME]?.asItemValueOrNull()

        val bank: String?
            get() = data[PaymentCreditCardXml.BANK]?.asItemValueOrNull()

        val color: Color?
            get() = data[PaymentCreditCardXml.COLOR]?.asEnumValue()

        val issueNumber: String?
            get() = data[PaymentCreditCardXml.ISSUE_NUMBER]?.asItemValueOrNull()

        val expireMonth: Month?
            get() = data[PaymentCreditCardXml.EXPIRE_MONTH]?.asItemValueOrNull()?.toMonthValueOrNull()

        val expireYear: Year?
            get() = data[PaymentCreditCardXml.EXPIRE_YEAR]?.asItemValueOrNull()?.toYearValueOrNull()

        val name: String?
            get() = data[PaymentCreditCardXml.NAME]?.asItemValueOrNull()

        val startMonth: Month?
            get() = data[PaymentCreditCardXml.START_MONTH]?.asItemValueOrNull()?.toMonthValueOrNull()

        val pin: SyncObfuscatedValue?
            get() = data[PaymentCreditCardXml.PIN]?.asItemNodeObfuscatedValueOrNull()

        val linkedBillingAddress: String?
            get() = data[PaymentCreditCardXml.LINKED_BILLING_ADDRESS]?.asItemValueOrNull()

        val startYear: Year?
            get() = data[PaymentCreditCardXml.START_YEAR]?.asItemValueOrNull()?.toYearValueOrNull()

        val cCNote: SyncObfuscatedValue?
            get() = data[PaymentCreditCardXml.C_C_NOTE]?.asItemNodeObfuscatedValueOrNull()

        val securityCode: SyncObfuscatedValue?
            get() = data[PaymentCreditCardXml.SECURITY_CODE]?.asItemNodeObfuscatedValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): PaymentCreditCard =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): PaymentCreditCard =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var cardNumber: SyncObfuscatedValue?
                get() = data[PaymentCreditCardXml.CARD_NUMBER]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.CARD_NUMBER] = value?.toItemNodeObfuscated()
                }

            var ownerName: String?
                get() = data[PaymentCreditCardXml.OWNER_NAME]?.asItemValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.OWNER_NAME] = value?.toItemNode()
                }

            var bank: String?
                get() = data[PaymentCreditCardXml.BANK]?.asItemValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.BANK] = value?.toItemNode()
                }

            var color: Color?
                get() = data[PaymentCreditCardXml.COLOR]?.asEnumValue()
                set(value) {
                    data[PaymentCreditCardXml.COLOR] = value?.toItemNodeFromEnum()
                }

            var issueNumber: String?
                get() = data[PaymentCreditCardXml.ISSUE_NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.ISSUE_NUMBER] = value?.toItemNode()
                }

            var expireMonth: Month?
                get() = data[PaymentCreditCardXml.EXPIRE_MONTH]?.asItemValueOrNull()?.toMonthValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.EXPIRE_MONTH] = value?.toXmlString()?.toItemNode()
                }

            var expireYear: Year?
                get() = data[PaymentCreditCardXml.EXPIRE_YEAR]?.asItemValueOrNull()?.toYearValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.EXPIRE_YEAR] = value?.toXmlString()?.toItemNode()
                }

            var name: String?
                get() = data[PaymentCreditCardXml.NAME]?.asItemValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.NAME] = value?.toItemNode()
                }

            var startMonth: Month?
                get() = data[PaymentCreditCardXml.START_MONTH]?.asItemValueOrNull()?.toMonthValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.START_MONTH] = value?.toXmlString()?.toItemNode()
                }

            var pin: SyncObfuscatedValue?
                get() = data[PaymentCreditCardXml.PIN]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.PIN] = value?.toItemNodeObfuscated()
                }

            var linkedBillingAddress: String?
                get() = data[PaymentCreditCardXml.LINKED_BILLING_ADDRESS]?.asItemValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.LINKED_BILLING_ADDRESS] = value?.toItemNode()
                }

            var startYear: Year?
                get() = data[PaymentCreditCardXml.START_YEAR]?.asItemValueOrNull()?.toYearValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.START_YEAR] = value?.toXmlString()?.toItemNode()
                }

            var cCNote: SyncObfuscatedValue?
                get() = data[PaymentCreditCardXml.C_C_NOTE]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.C_C_NOTE] = value?.toItemNodeObfuscated()
                }

            var securityCode: SyncObfuscatedValue?
                get() = data[PaymentCreditCardXml.SECURITY_CODE]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[PaymentCreditCardXml.SECURITY_CODE] = value?.toItemNodeObfuscated()
                }

            override fun build(): PaymentCreditCard =
                PaymentCreditCard(data.toMap())
        }

        enum class Color(override val value: String) : SyncObjectEnum {
            @Json(name = "RED")
            RED("RED"),

            @Json(name = "GOLD")
            GOLD("GOLD"),

            @Json(name = "GREEN_1")
            GREEN_1("GREEN_1"),

            @Json(name = "WHITE")
            WHITE("WHITE"),

            @Json(name = "GREEN_2")
            GREEN_2("GREEN_2"),

            @Json(name = "SILVER")
            SILVER("SILVER"),

            @Json(name = "BLACK")
            BLACK("BLACK"),

            @Json(name = "BLUE_1")
            BLUE_1("BLUE_1"),

            @Json(name = "BLUE_2")
            BLUE_2("BLUE_2"),

            @Json(name = "NO_TYPE")
            NO_TYPE("NO_TYPE"),

            @Json(name = "ORANGE")
            ORANGE("ORANGE"),
        }
    }

    @Generated
    data class PersonalWebsite(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.personalWebsite }.filterValues { !it.isEmpty() }

        val website: String?
            get() = data[PersonalWebsiteXml.WEBSITE]?.asItemValueOrNull()

        val name: String?
            get() = data[PersonalWebsiteXml.NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): PersonalWebsite =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): PersonalWebsite =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var website: String?
                get() = data[PersonalWebsiteXml.WEBSITE]?.asItemValueOrNull()
                set(value) {
                    data[PersonalWebsiteXml.WEBSITE] = value?.toItemNode()
                }

            var name: String?
                get() = data[PersonalWebsiteXml.NAME]?.asItemValueOrNull()
                set(value) {
                    data[PersonalWebsiteXml.NAME] = value?.toItemNode()
                }

            override fun build(): PersonalWebsite =
                PersonalWebsite(data.toMap())
        }
    }

    @Generated
    data class Phone(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.phone }.filterValues { !it.isEmpty() }

        val phoneName: String?
            get() = data[PhoneXml.PHONE_NAME]?.asItemValueOrNull()

        val type: Type?
            get() = data[PhoneXml.TYPE]?.asEnumValue()

        val number: String?
            get() = data[PhoneXml.NUMBER]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Phone =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Phone =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var phoneName: String?
                get() = data[PhoneXml.PHONE_NAME]?.asItemValueOrNull()
                set(value) {
                    data[PhoneXml.PHONE_NAME] = value?.toItemNode()
                }

            var type: Type?
                get() = data[PhoneXml.TYPE]?.asEnumValue()
                set(value) {
                    data[PhoneXml.TYPE] = value?.toItemNodeFromEnum()
                }

            var number: String?
                get() = data[PhoneXml.NUMBER]?.asItemValueOrNull()
                set(value) {
                    data[PhoneXml.NUMBER] = value?.toItemNode()
                }

            override fun build(): Phone =
                Phone(data.toMap())
        }

        enum class Type(override val value: String) : SyncObjectEnum {
            @Json(name = "PHONE_TYPE_MOBILE")
            PHONE_TYPE_MOBILE("PHONE_TYPE_MOBILE"),

            @Json(name = "PHONE_TYPE_WORK_LANDLINE")
            PHONE_TYPE_WORK_LANDLINE("PHONE_TYPE_WORK_LANDLINE"),

            @Json(name = "PHONE_TYPE_WORK_MOBILE")
            PHONE_TYPE_WORK_MOBILE("PHONE_TYPE_WORK_MOBILE"),

            @Json(name = "PHONE_TYPE_WORK_FAX")
            PHONE_TYPE_WORK_FAX("PHONE_TYPE_WORK_FAX"),

            @Json(name = "PHONE_TYPE_FAX")
            PHONE_TYPE_FAX("PHONE_TYPE_FAX"),

            @Json(name = "PHONE_TYPE_LANDLINE")
            PHONE_TYPE_LANDLINE("PHONE_TYPE_LANDLINE"),

            @Json(name = "PHONE_TYPE_ANY")
            PHONE_TYPE_ANY("PHONE_TYPE_ANY"),
        }
    }

    @Generated
    data class Secret(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.secret }.filterValues { !it.isEmpty() }

        val secured: Boolean?
            get() = data[SecretXml.SECURED]?.asItemValueOrNull()?.toBooleanValue()

        val content: String?
            get() = data[SecretXml.CONTENT]?.asItemValueOrNull()

        val title: String?
            get() = data[SecretXml.TITLE]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): Secret =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): Secret =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var secured: Boolean?
                get() = data[SecretXml.SECURED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SecretXml.SECURED] = value?.toString()?.toItemNode()
                }

            var content: String?
                get() = data[SecretXml.CONTENT]?.asItemValueOrNull()
                set(value) {
                    data[SecretXml.CONTENT] = value?.toItemNode()
                }

            var title: String?
                get() = data[SecretXml.TITLE]?.asItemValueOrNull()
                set(value) {
                    data[SecretXml.TITLE] = value?.toItemNode()
                }

            override fun build(): Secret =
                Secret(data.toMap())
        }
    }

    @Generated
    data class SecureFileInfo(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val type: String?
            get() = data[SecureFileInfoXml.TYPE]?.asItemValueOrNull()

        val owner: String?
            get() = data[SecureFileInfoXml.OWNER]?.asItemValueOrNull()

        val remoteSize: Long?
            get() = data[SecureFileInfoXml.REMOTE_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()

        val version: String?
            get() = data[SecureFileInfoXml.VERSION]?.asItemValueOrNull()

        val localSize: Long?
            get() = data[SecureFileInfoXml.LOCAL_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()

        val cryptoKey: String?
            get() = data[SecureFileInfoXml.CRYPTO_KEY]?.asItemValueOrNull()

        val filename: String?
            get() = data[SecureFileInfoXml.FILENAME]?.asItemValueOrNull()

        val downloadKey: String?
            get() = data[SecureFileInfoXml.DOWNLOAD_KEY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): SecureFileInfo =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): SecureFileInfo =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var type: String?
                get() = data[SecureFileInfoXml.TYPE]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.TYPE] = value?.toItemNode()
                }

            var owner: String?
                get() = data[SecureFileInfoXml.OWNER]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.OWNER] = value?.toItemNode()
                }

            var remoteSize: Long?
                get() = data[SecureFileInfoXml.REMOTE_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.REMOTE_SIZE] = value?.toString()?.toItemNode()
                }

            var version: String?
                get() = data[SecureFileInfoXml.VERSION]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.VERSION] = value?.toItemNode()
                }

            var localSize: Long?
                get() = data[SecureFileInfoXml.LOCAL_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.LOCAL_SIZE] = value?.toString()?.toItemNode()
                }

            var cryptoKey: String?
                get() = data[SecureFileInfoXml.CRYPTO_KEY]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.CRYPTO_KEY] = value?.toItemNode()
                }

            var filename: String?
                get() = data[SecureFileInfoXml.FILENAME]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.FILENAME] = value?.toItemNode()
                }

            var downloadKey: String?
                get() = data[SecureFileInfoXml.DOWNLOAD_KEY]?.asItemValueOrNull()
                set(value) {
                    data[SecureFileInfoXml.DOWNLOAD_KEY] = value?.toItemNode()
                }

            override fun build(): SecureFileInfo =
                SecureFileInfo(data.toMap())
        }
    }

    @Generated
    data class SecureNote(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.secureNote }.filterValues { !it.isEmpty() }

        val creationDate: Instant?
            get() = data[SecureNoteXml.CREATION_DATE]?.asItemValueOrNull()?.toInstantOrNull()

        val updateDate: Instant?
            get() = data[SecureNoteXml.UPDATE_DATE]?.asItemValueOrNull()?.toInstantOrNull()

        val type: SecureNoteType?
            get() = data[SecureNoteXml.TYPE]?.asEnumValue()

        val category: String?
            get() = data[SecureNoteXml.CATEGORY]?.asItemValueOrNull()

        val secured: Boolean?
            get() = data[SecureNoteXml.SECURED]?.asItemValueOrNull()?.toBooleanValue()

        val content: String?
            get() = data[SecureNoteXml.CONTENT]?.asItemValueOrNull()

        val title: String?
            get() = data[SecureNoteXml.TITLE]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): SecureNote =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): SecureNote =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var creationDate: Instant?
                get() = data[SecureNoteXml.CREATION_DATE]?.asItemValueOrNull()?.toInstantOrNull()
                set(value) {
                    data[SecureNoteXml.CREATION_DATE] = value?.toXmlString()?.toItemNode()
                }

            var updateDate: Instant?
                get() = data[SecureNoteXml.UPDATE_DATE]?.asItemValueOrNull()?.toInstantOrNull()
                set(value) {
                    data[SecureNoteXml.UPDATE_DATE] = value?.toXmlString()?.toItemNode()
                }

            var type: SecureNoteType?
                get() = data[SecureNoteXml.TYPE]?.asEnumValue()
                set(value) {
                    data[SecureNoteXml.TYPE] = value?.toItemNodeFromEnum()
                }

            var category: String?
                get() = data[SecureNoteXml.CATEGORY]?.asItemValueOrNull()
                set(value) {
                    data[SecureNoteXml.CATEGORY] = value?.toItemNode()
                }

            var secured: Boolean?
                get() = data[SecureNoteXml.SECURED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SecureNoteXml.SECURED] = value?.toString()?.toItemNode()
                }

            var content: String?
                get() = data[SecureNoteXml.CONTENT]?.asItemValueOrNull()
                set(value) {
                    data[SecureNoteXml.CONTENT] = value?.toItemNode()
                }

            var title: String?
                get() = data[SecureNoteXml.TITLE]?.asItemValueOrNull()
                set(value) {
                    data[SecureNoteXml.TITLE] = value?.toItemNode()
                }

            override fun build(): SecureNote =
                SecureNote(data.toMap())
        }
    }

    @Generated
    data class SecureNoteCategory(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.secureNoteCategory }.filterValues { !it.isEmpty() }

        val categoryName: String?
            get() = data[SecureNoteCategoryXml.CATEGORY_NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): SecureNoteCategory =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): SecureNoteCategory =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var categoryName: String?
                get() = data[SecureNoteCategoryXml.CATEGORY_NAME]?.asItemValueOrNull()
                set(value) {
                    data[SecureNoteCategoryXml.CATEGORY_NAME] = value?.toItemNode()
                }

            override fun build(): SecureNoteCategory =
                SecureNoteCategory(data.toMap())
        }
    }

    @Generated
    data class SecurityBreach(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val status: Status?
            get() = data[SecurityBreachXml.STATUS]?.asEnumValue()

        val leakedPasswords: SyncObfuscatedValue?
            get() = data[SecurityBreachXml.LEAKED_PASSWORDS]?.asItemNodeObfuscatedValueOrNull()

        val content: String?
            get() = data[SecurityBreachXml.CONTENT]?.asItemValueOrNull()

        val contentRevision: Long?
            get() = data[SecurityBreachXml.CONTENT_REVISION]?.asItemValueOrNull()?.toLongValueOrNull()

        val breachId: String?
            get() = data[SecurityBreachXml.BREACH_ID]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): SecurityBreach =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): SecurityBreach =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var status: Status?
                get() = data[SecurityBreachXml.STATUS]?.asEnumValue()
                set(value) {
                    data[SecurityBreachXml.STATUS] = value?.toItemNodeFromEnum()
                }

            var leakedPasswords: SyncObfuscatedValue?
                get() = data[SecurityBreachXml.LEAKED_PASSWORDS]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[SecurityBreachXml.LEAKED_PASSWORDS] = value?.toItemNodeObfuscated()
                }

            var content: String?
                get() = data[SecurityBreachXml.CONTENT]?.asItemValueOrNull()
                set(value) {
                    data[SecurityBreachXml.CONTENT] = value?.toItemNode()
                }

            var contentRevision: Long?
                get() = data[SecurityBreachXml.CONTENT_REVISION]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SecurityBreachXml.CONTENT_REVISION] = value?.toString()?.toItemNode()
                }

            var breachId: String?
                get() = data[SecurityBreachXml.BREACH_ID]?.asItemValueOrNull()
                set(value) {
                    data[SecurityBreachXml.BREACH_ID] = value?.toItemNode()
                }

            override fun build(): SecurityBreach =
                SecurityBreach(data.toMap())
        }

        enum class Status(override val value: String) : SyncObjectEnum {
            @Json(name = "VIEWED")
            VIEWED("VIEWED"),

            @Json(name = "ACKNOWLEDGED")
            ACKNOWLEDGED("ACKNOWLEDGED"),

            @Json(name = "SOLVED")
            SOLVED("SOLVED"),

            @Json(name = "PENDING")
            PENDING("PENDING"),
        }
    }

    @Generated
    data class Settings(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>?
            get() = null

        val gettingStartedPasswordsEnded: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_PASSWORDS_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val paymentsViewCriteria: String?
            get() = data[SettingsXml.PAYMENTS_VIEW_CRITERIA]?.asItemValueOrNull()

        val syncBackupPurchase: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP_PURCHASE]?.asItemValueOrNull()?.toBooleanValue()

        val syncTimer: Long?
            get() = data[SettingsXml.SYNC_TIMER]?.asItemValueOrNull()?.toLongValueOrNull()

        val spacesPresentationShown: Boolean?
            get() = data[SettingsXml.SPACES_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val defaultScreenEmergencyEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_EMERGENCY_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val purchasesViewCriteria: String?
            get() = data[SettingsXml.PURCHASES_VIEW_CRITERIA]?.asItemValueOrNull()

        val dashlaneName: String?
            get() = data[SettingsXml.DASHLANE_NAME]?.asItemValueOrNull()

        val sharingFacebookSessionSaved: Boolean?
            get() = data[SettingsXml.SHARING_FACEBOOK_SESSION_SAVED]?.asItemValueOrNull()?.toBooleanValue()

        val passwordLeakLastCheckedTimestamp: Long?
            get() = data[SettingsXml.PASSWORD_LEAK_LAST_CHECKED_TIMESTAMP]?.asItemValueOrNull()?.toLongValueOrNull()

        val defaultScreenWebPasswordsEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_WEB_PASSWORDS_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val notificationPurchases: Boolean?
            get() = data[SettingsXml.NOTIFICATION_PURCHASES]?.asItemValueOrNull()?.toBooleanValue()

        val generatorDefaultSymbols: Boolean?
            get() = data[SettingsXml.GENERATOR_DEFAULT_SYMBOLS]?.asItemValueOrNull()?.toBooleanValue()

        val disabledUrlsList: List<String>?
            get() = data[SettingsXml.DISABLED_URLS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

        val spacesRevokedShown: Boolean?
            get() = data[SettingsXml.SPACES_REVOKED_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val notificationSecurity: Boolean?
            get() = data[SettingsXml.NOTIFICATION_SECURITY]?.asItemValueOrNull()?.toBooleanValue()

        val prremiumShowBadges: Boolean?
            get() = data[SettingsXml.PRREMIUM_SHOW_BADGES]?.asItemValueOrNull()?.toBooleanValue()

        val deliveryType: String?
            get() = data[SettingsXml.DELIVERY_TYPE]?.asItemValueOrNull()

        val recoveryKey: ByteArray?
            get() = data[SettingsXml.RECOVERY_KEY]?.asItemValueOrNull()?.decodeBase64()

        val securityPhone: String?
            get() = data[SettingsXml.SECURITY_PHONE]?.asItemValueOrNull()

        val languageNoticesShown: String?
            get() = data[SettingsXml.LANGUAGE_NOTICES_SHOWN]?.asItemValueOrNull()

        val notificationPasswords: Boolean?
            get() = data[SettingsXml.NOTIFICATION_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()

        val securityEmail: String?
            get() = data[SettingsXml.SECURITY_EMAIL]?.asItemValueOrNull()

        val showAddObjects: String?
            get() = data[SettingsXml.SHOW_ADD_OBJECTS]?.asItemValueOrNull()

        val gettingStartedPersonaldataEnded: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_PERSONALDATA_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val oTPActivation: Long?
            get() = data[SettingsXml.O_T_P_ACTIVATION]?.asItemValueOrNull()?.toLongValueOrNull()

        val sharingTwitterSessionSaved: Boolean?
            get() = data[SettingsXml.SHARING_TWITTER_SESSION_SAVED]?.asItemValueOrNull()?.toBooleanValue()

        val renewalNoticeThirtyDaysShown: Boolean?
            get() = data[SettingsXml.RENEWAL_NOTICE_THIRTY_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val iDsViewCriteria: String?
            get() = data[SettingsXml.I_DS_VIEW_CRITERIA]?.asItemValueOrNull()

        val renewalNoticeLastDayShown: Boolean?
            get() = data[SettingsXml.RENEWAL_NOTICE_LAST_DAY_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val purchaseDefaultPaymentMean: String?
            get() = data[SettingsXml.PURCHASE_DEFAULT_PAYMENT_MEAN]?.asItemValueOrNull()

        val saveDataCapturedEnabled: String?
            get() = data[SettingsXml.SAVE_DATA_CAPTURED_ENABLED]?.asItemValueOrNull()

        val defaultScreenContactInformationEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_CONTACT_INFORMATION_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val purchasesSortCriteria: String?
            get() = data[SettingsXml.PURCHASES_SORT_CRITERIA]?.asItemValueOrNull()

        val sharingTwitterAccessToken: String?
            get() = data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN]?.asItemValueOrNull()

        val cryptoFixedSalt: ByteArray?
            get() = data[SettingsXml.CRYPTO_FIXED_SALT]?.asItemValueOrNull()?.decodeBase64()

        val purchaseDefaultAddress: String?
            get() = data[SettingsXml.PURCHASE_DEFAULT_ADDRESS]?.asItemValueOrNull()

        val syncBackup: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP]?.asItemValueOrNull()?.toBooleanValue()

        val recoveryHash: String?
            get() = data[SettingsXml.RECOVERY_HASH]?.asItemValueOrNull()

        val paymentsSortCriteria: String?
            get() = data[SettingsXml.PAYMENTS_SORT_CRITERIA]?.asItemValueOrNull()

        val vaultViewCriteria: String?
            get() = data[SettingsXml.VAULT_VIEW_CRITERIA]?.asItemValueOrNull()

        val purchaseGeneratePassword: Boolean?
            get() = data[SettingsXml.PURCHASE_GENERATE_PASSWORD]?.asItemValueOrNull()?.toBooleanValue()

        val countSupportUsPopupShown: Long?
            get() = data[SettingsXml.COUNT_SUPPORT_US_POPUP_SHOWN]?.asItemValueOrNull()?.toLongValueOrNull()

        val hotspotCredentialPasswordChangerComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_CREDENTIAL_PASSWORD_CHANGER_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val generatorDefaultDigits: Boolean?
            get() = data[SettingsXml.GENERATOR_DEFAULT_DIGITS]?.asItemValueOrNull()?.toBooleanValue()

        val defaultScreenPaymentMeansEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_PAYMENT_MEANS_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val mustFlushPurchasesDetailsLogs2: Boolean?
            get() = data[SettingsXml.MUST_FLUSH_PURCHASES_DETAILS_LOGS2]?.asItemValueOrNull()?.toBooleanValue()

        val disabledUrlsAutologinList: List<String>?
            get() = data[SettingsXml.DISABLED_URLS_AUTOLOGIN_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

        val purchaseDefaultPhone: String?
            get() = data[SettingsXml.PURCHASE_DEFAULT_PHONE]?.asItemValueOrNull()

        val autologoutInactivityException: String?
            get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY_EXCEPTION]?.asItemValueOrNull()

        val accountCreationDatetime: Long?
            get() = data[SettingsXml.ACCOUNT_CREATION_DATETIME]?.asItemValueOrNull()?.toLongValueOrNull()

        val vaultSortCriteria: String?
            get() = data[SettingsXml.VAULT_SORT_CRITERIA]?.asItemValueOrNull()

        val protectPayments: Boolean?
            get() = data[SettingsXml.PROTECT_PAYMENTS]?.asItemValueOrNull()?.toBooleanValue()

        val gettingStartedPasswordsImported: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_PASSWORDS_IMPORTED]?.asItemValueOrNull()?.toBooleanValue()

        val autofillSettings: AutofillSettings?
            get() = data[SettingsXml.AUTOFILL_SETTINGS]?.asJsonDataClassOrNull()

        val usagelogToken: String?
            get() = data[SettingsXml.USAGELOG_TOKEN]?.asItemValueOrNull()

        val autologoutInactivityDuration: String?
            get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY_DURATION]?.asItemValueOrNull()

        val generatorDefaultSize: Long?
            get() = data[SettingsXml.GENERATOR_DEFAULT_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()

        val defaultScreenSecuredNotesEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_SECURED_NOTES_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val hotspotSecureNoteShareComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_SECURE_NOTE_SHARE_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val richIcons: Boolean?
            get() = data[SettingsXml.RICH_ICONS]?.asItemValueOrNull()?.toBooleanValue()

        val accountRecoveryKey: String?
            get() = data[SettingsXml.ACCOUNT_RECOVERY_KEY]?.asItemValueOrNull()

        val syncBackupCreditCardsNumber: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_NUMBER]?.asItemValueOrNull()?.toBooleanValue()

        val protectPasswords: Boolean?
            get() = data[SettingsXml.PROTECT_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()

        val sortCriteria: String?
            get() = data[SettingsXml.SORT_CRITERIA]?.asItemValueOrNull()

        val banishedUrlsList: List<String>?
            get() = data[SettingsXml.BANISHED_URLS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

        val showLogoutConfirmation: Boolean?
            get() = data[SettingsXml.SHOW_LOGOUT_CONFIRMATION]?.asItemValueOrNull()?.toBooleanValue()

        val defaultScreenCivilInformationEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_CIVIL_INFORMATION_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val notificationSync: Boolean?
            get() = data[SettingsXml.NOTIFICATION_SYNC]?.asItemValueOrNull()?.toBooleanValue()

        val defaultScreenPurchasesAllEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_PURCHASES_ALL_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val gettingStartedSecurityDashboardEnded: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_SECURITY_DASHBOARD_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val showSystemTray: Boolean?
            get() = data[SettingsXml.SHOW_SYSTEM_TRAY]?.asItemValueOrNull()?.toBooleanValue()

        val renewalNotificationWillExpireLastDate: Long?
            get() = data[SettingsXml.RENEWAL_NOTIFICATION_WILL_EXPIRE_LAST_DATE]?.asItemValueOrNull()?.toLongValueOrNull()

        val securedDataShowScreenshots: Boolean?
            get() = data[SettingsXml.SECURED_DATA_SHOW_SCREENSHOTS]?.asItemValueOrNull()?.toBooleanValue()

        val syncBackupPersonaldata: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP_PERSONALDATA]?.asItemValueOrNull()?.toBooleanValue()

        val defaultScreenSharingEnded: Boolean?
            get() = data[SettingsXml.DEFAULT_SCREEN_SHARING_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val mustFlushPasswordsDetailsLogs: Boolean?
            get() = data[SettingsXml.MUST_FLUSH_PASSWORDS_DETAILS_LOGS]?.asItemValueOrNull()?.toBooleanValue()

        val threePointZeroPresentationShown: String?
            get() = data[SettingsXml.THREE_POINT_ZERO_PRESENTATION_SHOWN]?.asItemValueOrNull()

        val saveDataCapturedAutomatically: String?
            get() = data[SettingsXml.SAVE_DATA_CAPTURED_AUTOMATICALLY]?.asItemValueOrNull()

        val renewalNoticeFifteenDaysShown: Boolean?
            get() = data[SettingsXml.RENEWAL_NOTICE_FIFTEEN_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val passwordLeakAnonymousId: String?
            get() = data[SettingsXml.PASSWORD_LEAK_ANONYMOUS_ID]?.asItemValueOrNull()

        val hotspotTeamSpacesOtherVaultComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_TEAM_SPACES_OTHER_VAULT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val accountRecoveryKeyId: String?
            get() = data[SettingsXml.ACCOUNT_RECOVERY_KEY_ID]?.asItemValueOrNull()

        val hotspotCredentialSortComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_CREDENTIAL_SORT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val renewalNoticeExpiredShown: Boolean?
            get() = data[SettingsXml.RENEWAL_NOTICE_EXPIRED_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val dashlane6PresentationShown: Boolean?
            get() = data[SettingsXml.DASHLANE6_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val cryptoUserPayload: String?
            get() = data[SettingsXml.CRYPTO_USER_PAYLOAD]?.asItemValueOrNull()

        val spaceSettingsPresentationShown: Boolean?
            get() = data[SettingsXml.SPACE_SETTINGS_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val syncBackupPasswords: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()

        val hotspotCredentialShareComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_CREDENTIAL_SHARE_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val sharingFacebookUid: String?
            get() = data[SettingsXml.SHARING_FACEBOOK_UID]?.asItemValueOrNull()

        val spaceAnonIds: String?
            get() = data[SettingsXml.SPACE_ANON_IDS]?.asItemValueOrNull()

        val gettingStartedStepNewStatus1: String?
            get() = data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_1]?.asItemValueOrNull()

        val hotspotCredentialAddComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_CREDENTIAL_ADD_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val autologoutInactivity: String?
            get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY]?.asItemValueOrNull()

        val disabledDomainsList: List<String>?
            get() = data[SettingsXml.DISABLED_DOMAINS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

        val recoveryOptIn: Boolean?
            get() = data[SettingsXml.RECOVERY_OPT_IN]?.asItemValueOrNull()?.toBooleanValue()

        val protectIDs: Boolean?
            get() = data[SettingsXml.PROTECT_I_DS]?.asItemValueOrNull()?.toBooleanValue()

        val gettingStartedStepNewStatus3: String?
            get() = data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_3]?.asItemValueOrNull()

        val syncBackupCreditCardsCCV: Boolean?
            get() = data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_C_C_V]?.asItemValueOrNull()?.toBooleanValue()

        val hotspotTeamSpacesChangeVaultComplete: Boolean?
            get() = data[SettingsXml.HOTSPOT_TEAM_SPACES_CHANGE_VAULT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()

        val rsaPublicKey: String?
            get() = data[SettingsXml.RSA_PUBLIC_KEY]?.asItemValueOrNull()

        val sharingTwitterAccessTokenSecret: String?
            get() = data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN_SECRET]?.asItemValueOrNull()

        val passwordChangerSignupForBetaShown: Boolean?
            get() = data[SettingsXml.PASSWORD_CHANGER_SIGNUP_FOR_BETA_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val showImpalas: String?
            get() = data[SettingsXml.SHOW_IMPALAS]?.asItemValueOrNull()

        val contactsViewCriteria: String?
            get() = data[SettingsXml.CONTACTS_VIEW_CRITERIA]?.asItemValueOrNull()

        val gettingStartedVaultEnded: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_VAULT_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val generatorDefaultAvoidAmbiguousChars: Boolean?
            get() = data[SettingsXml.GENERATOR_DEFAULT_AVOID_AMBIGUOUS_CHARS]?.asItemValueOrNull()?.toBooleanValue()

        val anonymousUserId: String?
            get() = data[SettingsXml.ANONYMOUS_USER_ID]?.asItemValueOrNull()

        val disabledDomainsAutologinList: List<String>?
            get() = data[SettingsXml.DISABLED_DOMAINS_AUTOLOGIN_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }

        val generatorDefaultLetters: Boolean?
            get() = data[SettingsXml.GENERATOR_DEFAULT_LETTERS]?.asItemValueOrNull()?.toBooleanValue()

        val realLogin: String?
            get() = data[SettingsXml.REAL_LOGIN]?.asItemValueOrNull()

        val gettingStartedPopoverEnded: Boolean?
            get() = data[SettingsXml.GETTING_STARTED_POPOVER_ENDED]?.asItemValueOrNull()?.toBooleanValue()

        val renewalNotificationExpiredLastDate: Long?
            get() = data[SettingsXml.RENEWAL_NOTIFICATION_EXPIRED_LAST_DATE]?.asItemValueOrNull()?.toLongValueOrNull()

        val mustFlushCreditCardsDetailsLogs: Boolean?
            get() = data[SettingsXml.MUST_FLUSH_CREDIT_CARDS_DETAILS_LOGS]?.asItemValueOrNull()?.toBooleanValue()

        val credentialsViewCriteria: String?
            get() = data[SettingsXml.CREDENTIALS_VIEW_CRITERIA]?.asItemValueOrNull()

        val purchaseDefaultEmail: String?
            get() = data[SettingsXml.PURCHASE_DEFAULT_EMAIL]?.asItemValueOrNull()

        val iDsSortCriteria: String?
            get() = data[SettingsXml.I_DS_SORT_CRITERIA]?.asItemValueOrNull()

        val rsaPrivateKeys: String?
            get() = data[SettingsXml.RSA_PRIVATE_KEYS]?.asItemValueOrNull()

        val passwordChangerPublicAccessShown: Boolean?
            get() = data[SettingsXml.PASSWORD_CHANGER_PUBLIC_ACCESS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val sharingFacebookAccessToken: String?
            get() = data[SettingsXml.SHARING_FACEBOOK_ACCESS_TOKEN]?.asItemValueOrNull()

        val mobileAppLinkLastShownDate: Long?
            get() = data[SettingsXml.MOBILE_APP_LINK_LAST_SHOWN_DATE]?.asItemValueOrNull()?.toLongValueOrNull()

        val proxyActivation: Boolean?
            get() = data[SettingsXml.PROXY_ACTIVATION]?.asItemValueOrNull()?.toBooleanValue()

        val renewalNoticeFiveDaysShown: Boolean?
            get() = data[SettingsXml.RENEWAL_NOTICE_FIVE_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()

        val notificationPersonaldata: Boolean?
            get() = data[SettingsXml.NOTIFICATION_PERSONALDATA]?.asItemValueOrNull()?.toBooleanValue()

        inline fun copy(builder: Builder.() -> Unit = {}): Settings =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            fun create(
                anonymousUserId: String,
                cryptoUserPayload: String,
                usagelogToken: String,
            ) = Settings {
                this.anonymousUserId = anonymousUserId
                this.cryptoUserPayload = cryptoUserPayload
                this.usagelogToken = usagelogToken
                this.dashlane6PresentationShown = true
                this.dashlaneName = ""
                this.deliveryType = "DELIVERY_TYPE_NORMAL"
                this.gettingStartedPasswordsEnded = false
                this.gettingStartedPasswordsImported = false
                this.gettingStartedPersonaldataEnded = false
                this.gettingStartedPopoverEnded = false
                this.gettingStartedSecurityDashboardEnded = false
                this.notificationPasswords = true
                this.notificationPersonaldata = true
                this.notificationPurchases = true
                this.notificationSecurity = true
                this.notificationSync = true
                this.protectIDs = false
                this.protectPasswords = false
                this.protectPayments = true
                this.purchaseDefaultAddress = ""
                this.purchaseDefaultEmail = ""
                this.purchaseDefaultPaymentMean = ""
                this.purchaseDefaultPhone = ""
                this.purchaseGeneratePassword = true
                this.richIcons = true
                this.securedDataShowScreenshots = true
                this.showLogoutConfirmation = true
                this.showSystemTray = true
                this.syncBackup = true
            }

            inline operator fun invoke(builder: Builder.() -> Unit = {}): Settings =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var gettingStartedPasswordsEnded: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_PASSWORDS_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_PASSWORDS_ENDED] = value?.toString()?.toItemNode()
                }

            var paymentsViewCriteria: String?
                get() = data[SettingsXml.PAYMENTS_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PAYMENTS_VIEW_CRITERIA] = value?.toItemNode()
                }

            var syncBackupPurchase: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP_PURCHASE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP_PURCHASE] = value?.toString()?.toItemNode()
                }

            var syncTimer: Long?
                get() = data[SettingsXml.SYNC_TIMER]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.SYNC_TIMER] = value?.toString()?.toItemNode()
                }

            var spacesPresentationShown: Boolean?
                get() = data[SettingsXml.SPACES_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SPACES_PRESENTATION_SHOWN] = value?.toString()?.toItemNode()
                }

            var defaultScreenEmergencyEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_EMERGENCY_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_EMERGENCY_ENDED] = value?.toString()?.toItemNode()
                }

            var purchasesViewCriteria: String?
                get() = data[SettingsXml.PURCHASES_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASES_VIEW_CRITERIA] = value?.toItemNode()
                }

            var dashlaneName: String?
                get() = data[SettingsXml.DASHLANE_NAME]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.DASHLANE_NAME] = value?.toItemNode()
                }

            var sharingFacebookSessionSaved: Boolean?
                get() = data[SettingsXml.SHARING_FACEBOOK_SESSION_SAVED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SHARING_FACEBOOK_SESSION_SAVED] = value?.toString()?.toItemNode()
                }

            var passwordLeakLastCheckedTimestamp: Long?
                get() = data[SettingsXml.PASSWORD_LEAK_LAST_CHECKED_TIMESTAMP]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.PASSWORD_LEAK_LAST_CHECKED_TIMESTAMP] = value?.toString()?.toItemNode()
                }

            var defaultScreenWebPasswordsEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_WEB_PASSWORDS_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_WEB_PASSWORDS_ENDED] = value?.toString()?.toItemNode()
                }

            var notificationPurchases: Boolean?
                get() = data[SettingsXml.NOTIFICATION_PURCHASES]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.NOTIFICATION_PURCHASES] = value?.toString()?.toItemNode()
                }

            var generatorDefaultSymbols: Boolean?
                get() = data[SettingsXml.GENERATOR_DEFAULT_SYMBOLS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GENERATOR_DEFAULT_SYMBOLS] = value?.toString()?.toItemNode()
                }

            var disabledUrlsList: List<String>?
                get() = data[SettingsXml.DISABLED_URLS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                set(value) {
                    data[SettingsXml.DISABLED_URLS_LIST] = value?.map { it.toItemNode() }?.toListNode()
                }

            var spacesRevokedShown: Boolean?
                get() = data[SettingsXml.SPACES_REVOKED_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SPACES_REVOKED_SHOWN] = value?.toString()?.toItemNode()
                }

            var notificationSecurity: Boolean?
                get() = data[SettingsXml.NOTIFICATION_SECURITY]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.NOTIFICATION_SECURITY] = value?.toString()?.toItemNode()
                }

            var prremiumShowBadges: Boolean?
                get() = data[SettingsXml.PRREMIUM_SHOW_BADGES]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PRREMIUM_SHOW_BADGES] = value?.toString()?.toItemNode()
                }

            var deliveryType: String?
                get() = data[SettingsXml.DELIVERY_TYPE]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.DELIVERY_TYPE] = value?.toItemNode()
                }

            var recoveryKey: ByteArray?
                get() = data[SettingsXml.RECOVERY_KEY]?.asItemValueOrNull()?.decodeBase64()
                set(value) {
                    data[SettingsXml.RECOVERY_KEY] = value?.encodeBase64()?.toItemNode()
                }

            var securityPhone: String?
                get() = data[SettingsXml.SECURITY_PHONE]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SECURITY_PHONE] = value?.toItemNode()
                }

            var languageNoticesShown: String?
                get() = data[SettingsXml.LANGUAGE_NOTICES_SHOWN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.LANGUAGE_NOTICES_SHOWN] = value?.toItemNode()
                }

            var notificationPasswords: Boolean?
                get() = data[SettingsXml.NOTIFICATION_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.NOTIFICATION_PASSWORDS] = value?.toString()?.toItemNode()
                }

            var securityEmail: String?
                get() = data[SettingsXml.SECURITY_EMAIL]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SECURITY_EMAIL] = value?.toItemNode()
                }

            var showAddObjects: String?
                get() = data[SettingsXml.SHOW_ADD_OBJECTS]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHOW_ADD_OBJECTS] = value?.toItemNode()
                }

            var gettingStartedPersonaldataEnded: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_PERSONALDATA_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_PERSONALDATA_ENDED] = value?.toString()?.toItemNode()
                }

            var oTPActivation: Long?
                get() = data[SettingsXml.O_T_P_ACTIVATION]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.O_T_P_ACTIVATION] = value?.toString()?.toItemNode()
                }

            var sharingTwitterSessionSaved: Boolean?
                get() = data[SettingsXml.SHARING_TWITTER_SESSION_SAVED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SHARING_TWITTER_SESSION_SAVED] = value?.toString()?.toItemNode()
                }

            var renewalNoticeThirtyDaysShown: Boolean?
                get() = data[SettingsXml.RENEWAL_NOTICE_THIRTY_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTICE_THIRTY_DAYS_SHOWN] = value?.toString()?.toItemNode()
                }

            var iDsViewCriteria: String?
                get() = data[SettingsXml.I_DS_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.I_DS_VIEW_CRITERIA] = value?.toItemNode()
                }

            var renewalNoticeLastDayShown: Boolean?
                get() = data[SettingsXml.RENEWAL_NOTICE_LAST_DAY_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTICE_LAST_DAY_SHOWN] = value?.toString()?.toItemNode()
                }

            var purchaseDefaultPaymentMean: String?
                get() = data[SettingsXml.PURCHASE_DEFAULT_PAYMENT_MEAN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASE_DEFAULT_PAYMENT_MEAN] = value?.toItemNode()
                }

            var saveDataCapturedEnabled: String?
                get() = data[SettingsXml.SAVE_DATA_CAPTURED_ENABLED]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SAVE_DATA_CAPTURED_ENABLED] = value?.toItemNode()
                }

            var defaultScreenContactInformationEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_CONTACT_INFORMATION_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_CONTACT_INFORMATION_ENDED] = value?.toString()?.toItemNode()
                }

            var purchasesSortCriteria: String?
                get() = data[SettingsXml.PURCHASES_SORT_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASES_SORT_CRITERIA] = value?.toItemNode()
                }

            var sharingTwitterAccessToken: String?
                get() = data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN] = value?.toItemNode()
                }

            var cryptoFixedSalt: ByteArray?
                get() = data[SettingsXml.CRYPTO_FIXED_SALT]?.asItemValueOrNull()?.decodeBase64()
                set(value) {
                    data[SettingsXml.CRYPTO_FIXED_SALT] = value?.encodeBase64()?.toItemNode()
                }

            var purchaseDefaultAddress: String?
                get() = data[SettingsXml.PURCHASE_DEFAULT_ADDRESS]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASE_DEFAULT_ADDRESS] = value?.toItemNode()
                }

            var syncBackup: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP] = value?.toString()?.toItemNode()
                }

            var recoveryHash: String?
                get() = data[SettingsXml.RECOVERY_HASH]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.RECOVERY_HASH] = value?.toItemNode()
                }

            var paymentsSortCriteria: String?
                get() = data[SettingsXml.PAYMENTS_SORT_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PAYMENTS_SORT_CRITERIA] = value?.toItemNode()
                }

            var vaultViewCriteria: String?
                get() = data[SettingsXml.VAULT_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.VAULT_VIEW_CRITERIA] = value?.toItemNode()
                }

            var purchaseGeneratePassword: Boolean?
                get() = data[SettingsXml.PURCHASE_GENERATE_PASSWORD]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PURCHASE_GENERATE_PASSWORD] = value?.toString()?.toItemNode()
                }

            var countSupportUsPopupShown: Long?
                get() = data[SettingsXml.COUNT_SUPPORT_US_POPUP_SHOWN]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.COUNT_SUPPORT_US_POPUP_SHOWN] = value?.toString()?.toItemNode()
                }

            var hotspotCredentialPasswordChangerComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_CREDENTIAL_PASSWORD_CHANGER_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_CREDENTIAL_PASSWORD_CHANGER_COMPLETE] = value?.toString()?.toItemNode()
                }

            var generatorDefaultDigits: Boolean?
                get() = data[SettingsXml.GENERATOR_DEFAULT_DIGITS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GENERATOR_DEFAULT_DIGITS] = value?.toString()?.toItemNode()
                }

            var defaultScreenPaymentMeansEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_PAYMENT_MEANS_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_PAYMENT_MEANS_ENDED] = value?.toString()?.toItemNode()
                }

            var mustFlushPurchasesDetailsLogs2: Boolean?
                get() = data[SettingsXml.MUST_FLUSH_PURCHASES_DETAILS_LOGS2]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.MUST_FLUSH_PURCHASES_DETAILS_LOGS2] = value?.toString()?.toItemNode()
                }

            var disabledUrlsAutologinList: List<String>?
                get() = data[SettingsXml.DISABLED_URLS_AUTOLOGIN_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                set(value) {
                    data[SettingsXml.DISABLED_URLS_AUTOLOGIN_LIST] = value?.map { it.toItemNode() }?.toListNode()
                }

            var purchaseDefaultPhone: String?
                get() = data[SettingsXml.PURCHASE_DEFAULT_PHONE]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASE_DEFAULT_PHONE] = value?.toItemNode()
                }

            var autologoutInactivityException: String?
                get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY_EXCEPTION]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.AUTOLOGOUT_INACTIVITY_EXCEPTION] = value?.toItemNode()
                }

            var accountCreationDatetime: Long?
                get() = data[SettingsXml.ACCOUNT_CREATION_DATETIME]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.ACCOUNT_CREATION_DATETIME] = value?.toString()?.toItemNode()
                }

            var vaultSortCriteria: String?
                get() = data[SettingsXml.VAULT_SORT_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.VAULT_SORT_CRITERIA] = value?.toItemNode()
                }

            var protectPayments: Boolean?
                get() = data[SettingsXml.PROTECT_PAYMENTS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PROTECT_PAYMENTS] = value?.toString()?.toItemNode()
                }

            var gettingStartedPasswordsImported: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_PASSWORDS_IMPORTED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_PASSWORDS_IMPORTED] = value?.toString()?.toItemNode()
                }

            var autofillSettings: AutofillSettings?
                get() = data[SettingsXml.AUTOFILL_SETTINGS]?.asJsonDataClassOrNull()
                set(value) {
                    data[SettingsXml.AUTOFILL_SETTINGS] = value?.toItemNodeFromJson()
                }

            var usagelogToken: String?
                get() = data[SettingsXml.USAGELOG_TOKEN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.USAGELOG_TOKEN] = value?.toItemNode()
                }

            var autologoutInactivityDuration: String?
                get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY_DURATION]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.AUTOLOGOUT_INACTIVITY_DURATION] = value?.toItemNode()
                }

            var generatorDefaultSize: Long?
                get() = data[SettingsXml.GENERATOR_DEFAULT_SIZE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.GENERATOR_DEFAULT_SIZE] = value?.toString()?.toItemNode()
                }

            var defaultScreenSecuredNotesEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_SECURED_NOTES_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_SECURED_NOTES_ENDED] = value?.toString()?.toItemNode()
                }

            var hotspotSecureNoteShareComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_SECURE_NOTE_SHARE_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_SECURE_NOTE_SHARE_COMPLETE] = value?.toString()?.toItemNode()
                }

            var richIcons: Boolean?
                get() = data[SettingsXml.RICH_ICONS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RICH_ICONS] = value?.toString()?.toItemNode()
                }

            var accountRecoveryKey: String?
                get() = data[SettingsXml.ACCOUNT_RECOVERY_KEY]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.ACCOUNT_RECOVERY_KEY] = value?.toItemNode()
                }

            var syncBackupCreditCardsNumber: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_NUMBER]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_NUMBER] = value?.toString()?.toItemNode()
                }

            var protectPasswords: Boolean?
                get() = data[SettingsXml.PROTECT_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PROTECT_PASSWORDS] = value?.toString()?.toItemNode()
                }

            var sortCriteria: String?
                get() = data[SettingsXml.SORT_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SORT_CRITERIA] = value?.toItemNode()
                }

            var banishedUrlsList: List<String>?
                get() = data[SettingsXml.BANISHED_URLS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                set(value) {
                    data[SettingsXml.BANISHED_URLS_LIST] = value?.map { it.toItemNode() }?.toListNode()
                }

            var showLogoutConfirmation: Boolean?
                get() = data[SettingsXml.SHOW_LOGOUT_CONFIRMATION]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SHOW_LOGOUT_CONFIRMATION] = value?.toString()?.toItemNode()
                }

            var defaultScreenCivilInformationEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_CIVIL_INFORMATION_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_CIVIL_INFORMATION_ENDED] = value?.toString()?.toItemNode()
                }

            var notificationSync: Boolean?
                get() = data[SettingsXml.NOTIFICATION_SYNC]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.NOTIFICATION_SYNC] = value?.toString()?.toItemNode()
                }

            var defaultScreenPurchasesAllEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_PURCHASES_ALL_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_PURCHASES_ALL_ENDED] = value?.toString()?.toItemNode()
                }

            var gettingStartedSecurityDashboardEnded: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_SECURITY_DASHBOARD_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_SECURITY_DASHBOARD_ENDED] = value?.toString()?.toItemNode()
                }

            var showSystemTray: Boolean?
                get() = data[SettingsXml.SHOW_SYSTEM_TRAY]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SHOW_SYSTEM_TRAY] = value?.toString()?.toItemNode()
                }

            var renewalNotificationWillExpireLastDate: Long?
                get() = data[SettingsXml.RENEWAL_NOTIFICATION_WILL_EXPIRE_LAST_DATE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTIFICATION_WILL_EXPIRE_LAST_DATE] = value?.toString()?.toItemNode()
                }

            var securedDataShowScreenshots: Boolean?
                get() = data[SettingsXml.SECURED_DATA_SHOW_SCREENSHOTS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SECURED_DATA_SHOW_SCREENSHOTS] = value?.toString()?.toItemNode()
                }

            var syncBackupPersonaldata: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP_PERSONALDATA]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP_PERSONALDATA] = value?.toString()?.toItemNode()
                }

            var defaultScreenSharingEnded: Boolean?
                get() = data[SettingsXml.DEFAULT_SCREEN_SHARING_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DEFAULT_SCREEN_SHARING_ENDED] = value?.toString()?.toItemNode()
                }

            var mustFlushPasswordsDetailsLogs: Boolean?
                get() = data[SettingsXml.MUST_FLUSH_PASSWORDS_DETAILS_LOGS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.MUST_FLUSH_PASSWORDS_DETAILS_LOGS] = value?.toString()?.toItemNode()
                }

            var threePointZeroPresentationShown: String?
                get() = data[SettingsXml.THREE_POINT_ZERO_PRESENTATION_SHOWN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.THREE_POINT_ZERO_PRESENTATION_SHOWN] = value?.toItemNode()
                }

            var saveDataCapturedAutomatically: String?
                get() = data[SettingsXml.SAVE_DATA_CAPTURED_AUTOMATICALLY]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SAVE_DATA_CAPTURED_AUTOMATICALLY] = value?.toItemNode()
                }

            var renewalNoticeFifteenDaysShown: Boolean?
                get() = data[SettingsXml.RENEWAL_NOTICE_FIFTEEN_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTICE_FIFTEEN_DAYS_SHOWN] = value?.toString()?.toItemNode()
                }

            var passwordLeakAnonymousId: String?
                get() = data[SettingsXml.PASSWORD_LEAK_ANONYMOUS_ID]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PASSWORD_LEAK_ANONYMOUS_ID] = value?.toItemNode()
                }

            var hotspotTeamSpacesOtherVaultComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_TEAM_SPACES_OTHER_VAULT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_TEAM_SPACES_OTHER_VAULT_COMPLETE] = value?.toString()?.toItemNode()
                }

            var accountRecoveryKeyId: String?
                get() = data[SettingsXml.ACCOUNT_RECOVERY_KEY_ID]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.ACCOUNT_RECOVERY_KEY_ID] = value?.toItemNode()
                }

            var hotspotCredentialSortComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_CREDENTIAL_SORT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_CREDENTIAL_SORT_COMPLETE] = value?.toString()?.toItemNode()
                }

            var renewalNoticeExpiredShown: Boolean?
                get() = data[SettingsXml.RENEWAL_NOTICE_EXPIRED_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTICE_EXPIRED_SHOWN] = value?.toString()?.toItemNode()
                }

            var dashlane6PresentationShown: Boolean?
                get() = data[SettingsXml.DASHLANE6_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.DASHLANE6_PRESENTATION_SHOWN] = value?.toString()?.toItemNode()
                }

            var cryptoUserPayload: String?
                get() = data[SettingsXml.CRYPTO_USER_PAYLOAD]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.CRYPTO_USER_PAYLOAD] = value?.toItemNode()
                }

            var spaceSettingsPresentationShown: Boolean?
                get() = data[SettingsXml.SPACE_SETTINGS_PRESENTATION_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SPACE_SETTINGS_PRESENTATION_SHOWN] = value?.toString()?.toItemNode()
                }

            var syncBackupPasswords: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP_PASSWORDS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP_PASSWORDS] = value?.toString()?.toItemNode()
                }

            var hotspotCredentialShareComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_CREDENTIAL_SHARE_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_CREDENTIAL_SHARE_COMPLETE] = value?.toString()?.toItemNode()
                }

            var sharingFacebookUid: String?
                get() = data[SettingsXml.SHARING_FACEBOOK_UID]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHARING_FACEBOOK_UID] = value?.toItemNode()
                }

            var spaceAnonIds: String?
                get() = data[SettingsXml.SPACE_ANON_IDS]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SPACE_ANON_IDS] = value?.toItemNode()
                }

            var gettingStartedStepNewStatus1: String?
                get() = data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_1]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_1] = value?.toItemNode()
                }

            var hotspotCredentialAddComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_CREDENTIAL_ADD_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_CREDENTIAL_ADD_COMPLETE] = value?.toString()?.toItemNode()
                }

            var autologoutInactivity: String?
                get() = data[SettingsXml.AUTOLOGOUT_INACTIVITY]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.AUTOLOGOUT_INACTIVITY] = value?.toItemNode()
                }

            var disabledDomainsList: List<String>?
                get() = data[SettingsXml.DISABLED_DOMAINS_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                set(value) {
                    data[SettingsXml.DISABLED_DOMAINS_LIST] = value?.map { it.toItemNode() }?.toListNode()
                }

            var recoveryOptIn: Boolean?
                get() = data[SettingsXml.RECOVERY_OPT_IN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RECOVERY_OPT_IN] = value?.toString()?.toItemNode()
                }

            var protectIDs: Boolean?
                get() = data[SettingsXml.PROTECT_I_DS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PROTECT_I_DS] = value?.toString()?.toItemNode()
                }

            var gettingStartedStepNewStatus3: String?
                get() = data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_3]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_STEP_NEW_STATUS_3] = value?.toItemNode()
                }

            var syncBackupCreditCardsCCV: Boolean?
                get() = data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_C_C_V]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.SYNC_BACKUP_CREDIT_CARDS_C_C_V] = value?.toString()?.toItemNode()
                }

            var hotspotTeamSpacesChangeVaultComplete: Boolean?
                get() = data[SettingsXml.HOTSPOT_TEAM_SPACES_CHANGE_VAULT_COMPLETE]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.HOTSPOT_TEAM_SPACES_CHANGE_VAULT_COMPLETE] = value?.toString()?.toItemNode()
                }

            var rsaPublicKey: String?
                get() = data[SettingsXml.RSA_PUBLIC_KEY]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.RSA_PUBLIC_KEY] = value?.toItemNode()
                }

            var sharingTwitterAccessTokenSecret: String?
                get() = data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN_SECRET]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHARING_TWITTER_ACCESS_TOKEN_SECRET] = value?.toItemNode()
                }

            var passwordChangerSignupForBetaShown: Boolean?
                get() = data[SettingsXml.PASSWORD_CHANGER_SIGNUP_FOR_BETA_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PASSWORD_CHANGER_SIGNUP_FOR_BETA_SHOWN] = value?.toString()?.toItemNode()
                }

            var showImpalas: String?
                get() = data[SettingsXml.SHOW_IMPALAS]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHOW_IMPALAS] = value?.toItemNode()
                }

            var contactsViewCriteria: String?
                get() = data[SettingsXml.CONTACTS_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.CONTACTS_VIEW_CRITERIA] = value?.toItemNode()
                }

            var gettingStartedVaultEnded: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_VAULT_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_VAULT_ENDED] = value?.toString()?.toItemNode()
                }

            var generatorDefaultAvoidAmbiguousChars: Boolean?
                get() = data[SettingsXml.GENERATOR_DEFAULT_AVOID_AMBIGUOUS_CHARS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GENERATOR_DEFAULT_AVOID_AMBIGUOUS_CHARS] = value?.toString()?.toItemNode()
                }

            var anonymousUserId: String?
                get() = data[SettingsXml.ANONYMOUS_USER_ID]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.ANONYMOUS_USER_ID] = value?.toItemNode()
                }

            var disabledDomainsAutologinList: List<String>?
                get() = data[SettingsXml.DISABLED_DOMAINS_AUTOLOGIN_LIST]?.asListValueOrNull()?.mapNotNull { it.asItemValueOrNull() }
                set(value) {
                    data[SettingsXml.DISABLED_DOMAINS_AUTOLOGIN_LIST] = value?.map { it.toItemNode() }?.toListNode()
                }

            var generatorDefaultLetters: Boolean?
                get() = data[SettingsXml.GENERATOR_DEFAULT_LETTERS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GENERATOR_DEFAULT_LETTERS] = value?.toString()?.toItemNode()
                }

            var realLogin: String?
                get() = data[SettingsXml.REAL_LOGIN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.REAL_LOGIN] = value?.toItemNode()
                }

            var gettingStartedPopoverEnded: Boolean?
                get() = data[SettingsXml.GETTING_STARTED_POPOVER_ENDED]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.GETTING_STARTED_POPOVER_ENDED] = value?.toString()?.toItemNode()
                }

            var renewalNotificationExpiredLastDate: Long?
                get() = data[SettingsXml.RENEWAL_NOTIFICATION_EXPIRED_LAST_DATE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTIFICATION_EXPIRED_LAST_DATE] = value?.toString()?.toItemNode()
                }

            var mustFlushCreditCardsDetailsLogs: Boolean?
                get() = data[SettingsXml.MUST_FLUSH_CREDIT_CARDS_DETAILS_LOGS]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.MUST_FLUSH_CREDIT_CARDS_DETAILS_LOGS] = value?.toString()?.toItemNode()
                }

            var credentialsViewCriteria: String?
                get() = data[SettingsXml.CREDENTIALS_VIEW_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.CREDENTIALS_VIEW_CRITERIA] = value?.toItemNode()
                }

            var purchaseDefaultEmail: String?
                get() = data[SettingsXml.PURCHASE_DEFAULT_EMAIL]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.PURCHASE_DEFAULT_EMAIL] = value?.toItemNode()
                }

            var iDsSortCriteria: String?
                get() = data[SettingsXml.I_DS_SORT_CRITERIA]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.I_DS_SORT_CRITERIA] = value?.toItemNode()
                }

            var rsaPrivateKeys: String?
                get() = data[SettingsXml.RSA_PRIVATE_KEYS]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.RSA_PRIVATE_KEYS] = value?.toItemNode()
                }

            var passwordChangerPublicAccessShown: Boolean?
                get() = data[SettingsXml.PASSWORD_CHANGER_PUBLIC_ACCESS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PASSWORD_CHANGER_PUBLIC_ACCESS_SHOWN] = value?.toString()?.toItemNode()
                }

            var sharingFacebookAccessToken: String?
                get() = data[SettingsXml.SHARING_FACEBOOK_ACCESS_TOKEN]?.asItemValueOrNull()
                set(value) {
                    data[SettingsXml.SHARING_FACEBOOK_ACCESS_TOKEN] = value?.toItemNode()
                }

            var mobileAppLinkLastShownDate: Long?
                get() = data[SettingsXml.MOBILE_APP_LINK_LAST_SHOWN_DATE]?.asItemValueOrNull()?.toLongValueOrNull()
                set(value) {
                    data[SettingsXml.MOBILE_APP_LINK_LAST_SHOWN_DATE] = value?.toString()?.toItemNode()
                }

            var proxyActivation: Boolean?
                get() = data[SettingsXml.PROXY_ACTIVATION]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.PROXY_ACTIVATION] = value?.toString()?.toItemNode()
                }

            var renewalNoticeFiveDaysShown: Boolean?
                get() = data[SettingsXml.RENEWAL_NOTICE_FIVE_DAYS_SHOWN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.RENEWAL_NOTICE_FIVE_DAYS_SHOWN] = value?.toString()?.toItemNode()
                }

            var notificationPersonaldata: Boolean?
                get() = data[SettingsXml.NOTIFICATION_PERSONALDATA]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[SettingsXml.NOTIFICATION_PERSONALDATA] = value?.toString()?.toItemNode()
                }

            override fun build(): Settings =
                Settings(data.toMap())
        }

        @Generated
        @JsonClass(generateAdapter = true)
        data class AutofillSettings(
            @Json(name = "disabledSourceTypes")
            val disabledSourceTypes: List<DisabledSourceTypes>?,
            @Json(name = "disabledDomains")
            val disabledDomains: List<String>?,
            @Json(name = "isFollowUpNotificationEnabled")
            val isFollowUpNotificationEnabled: Boolean?,
            @Json(name = "isAutologinDisabled")
            val isAutologinDisabled: Boolean?,
            @Json(name = "isAutofillDisabled")
            val isAutofillDisabled: Boolean?,
        ) {
            @Generated
            @JsonClass(generateAdapter = true)
            data class DisabledSourceTypes(
                @Json(name = "Type")
                val type: AutofillDisabledSourceType?,
            )
        }
    }

    @Generated
    data class SocialSecurityStatement(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.socialSecurityStatement }.filterValues { !it.isEmpty() }

        val dateOfBirth: LocalDate?
            get() = data[SocialSecurityStatementXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()

        val socialSecurityNumber: SyncObfuscatedValue?
            get() = data[SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER]?.asItemNodeObfuscatedValueOrNull()

        val sex: Gender?
            get() = data[SocialSecurityStatementXml.SEX]?.asEnumValue()

        val socialSecurityFullname: String?
            get() = data[SocialSecurityStatementXml.SOCIAL_SECURITY_FULLNAME]?.asItemValueOrNull()

        val linkedIdentity: String?
            get() = data[SocialSecurityStatementXml.LINKED_IDENTITY]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): SocialSecurityStatement =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): SocialSecurityStatement =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var dateOfBirth: LocalDate?
                get() = data[SocialSecurityStatementXml.DATE_OF_BIRTH]?.asItemValueOrNull()?.toLocalDateOrNull()
                set(value) {
                    data[SocialSecurityStatementXml.DATE_OF_BIRTH] = value?.toXmlString()?.toItemNode()
                }

            var socialSecurityNumber: SyncObfuscatedValue?
                get() = data[SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER] = value?.toItemNodeObfuscated()
                }

            var sex: Gender?
                get() = data[SocialSecurityStatementXml.SEX]?.asEnumValue()
                set(value) {
                    data[SocialSecurityStatementXml.SEX] = value?.toItemNodeFromEnum()
                }

            var socialSecurityFullname: String?
                get() = data[SocialSecurityStatementXml.SOCIAL_SECURITY_FULLNAME]?.asItemValueOrNull()
                set(value) {
                    data[SocialSecurityStatementXml.SOCIAL_SECURITY_FULLNAME] = value?.toItemNode()
                }

            var linkedIdentity: String?
                get() = data[SocialSecurityStatementXml.LINKED_IDENTITY]?.asItemValueOrNull()
                set(value) {
                    data[SocialSecurityStatementXml.LINKED_IDENTITY] = value?.toItemNode()
                }

            override fun build(): SocialSecurityStatement =
                SocialSecurityStatement(data.toMap())
        }
    }

    @Generated
    data class WiFi(override val data: Map<String, XmlData>) : SyncObject() {
        override val userContentData: Map<String, XmlData>
            get() = data.filterKeys { it in SyncUserContentFields.wiFi }.filterValues { !it.isEmpty() }

        val encryptionType: WiFiEncryptionType?
            get() = data[WiFiXml.ENCRYPTION_TYPE]?.asEnumValue()

        val note: String?
            get() = data[WiFiXml.NOTE]?.asItemValueOrNull()

        val passphrase: SyncObfuscatedValue?
            get() = data[WiFiXml.PASSPHRASE]?.asItemNodeObfuscatedValueOrNull()

        val hidden: Boolean?
            get() = data[WiFiXml.HIDDEN]?.asItemValueOrNull()?.toBooleanValue()

        val ssid: String?
            get() = data[WiFiXml.SSID]?.asItemValueOrNull()

        val name: String?
            get() = data[WiFiXml.NAME]?.asItemValueOrNull()

        inline fun copy(builder: Builder.() -> Unit = {}): WiFi =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            inline operator fun invoke(builder: Builder.() -> Unit = {}): WiFi =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            var encryptionType: WiFiEncryptionType?
                get() = data[WiFiXml.ENCRYPTION_TYPE]?.asEnumValue()
                set(value) {
                    data[WiFiXml.ENCRYPTION_TYPE] = value?.toItemNodeFromEnum()
                }

            var note: String?
                get() = data[WiFiXml.NOTE]?.asItemValueOrNull()
                set(value) {
                    data[WiFiXml.NOTE] = value?.toItemNode()
                }

            var passphrase: SyncObfuscatedValue?
                get() = data[WiFiXml.PASSPHRASE]?.asItemNodeObfuscatedValueOrNull()
                set(value) {
                    data[WiFiXml.PASSPHRASE] = value?.toItemNodeObfuscated()
                }

            var hidden: Boolean?
                get() = data[WiFiXml.HIDDEN]?.asItemValueOrNull()?.toBooleanValue()
                set(value) {
                    data[WiFiXml.HIDDEN] = value?.toString()?.toItemNode()
                }

            var ssid: String?
                get() = data[WiFiXml.SSID]?.asItemValueOrNull()
                set(value) {
                    data[WiFiXml.SSID] = value?.toItemNode()
                }

            var name: String?
                get() = data[WiFiXml.NAME]?.asItemValueOrNull()
                set(value) {
                    data[WiFiXml.NAME] = value?.toItemNode()
                }

            override fun build(): WiFi =
                WiFi(data.toMap())
        }
    }

    abstract class Builder internal constructor(val data: MutableMap<String, XmlData> = mutableMapOf()) {
        var creationDatetime: Instant?
            get() = data[SyncObjectXml.CREATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()
            set(value) {
                data[SyncObjectXml.CREATION_DATETIME] = value?.toXmlString()?.toItemNode()
            }

        var localeFormat: Country?
            get() = data[SyncObjectXml.LOCALE_FORMAT]?.asItemValueOrNull()?.toCountryOrNull()
            set(value) {
                data[SyncObjectXml.LOCALE_FORMAT] = value?.isoCode?.toItemNode()
            }

        var spaceId: String?
            get() = data[SyncObjectXml.SPACE_ID]?.asItemValueOrNull()
            set(value) {
                data[SyncObjectXml.SPACE_ID] = value?.toItemNode()
            }

        var userModificationDatetime: Instant?
            get() = data[SyncObjectXml.USER_MODIFICATION_DATETIME]?.asItemValueOrNull()?.toInstantOrNull()
            set(value) {
                data[SyncObjectXml.USER_MODIFICATION_DATETIME] = value?.toXmlString()?.toItemNode()
            }

        var id: String?
            get() = data[SyncObjectXml.ID]?.asItemValueOrNull()
            set(value) {
                data[SyncObjectXml.ID] = value?.toItemNode()
            }

        var attachments: String?
            get() = data[SyncObjectXml.ATTACHMENTS]?.asItemValueOrNull()
            set(value) {
                data[SyncObjectXml.ATTACHMENTS] = value?.toItemNode()
            }

        var isFavorite: Boolean?
            get() = data[SyncObjectXml.IS_FAVORITE]?.asItemValueOrNull()?.toBooleanValue()
            set(value) {
                data[SyncObjectXml.IS_FAVORITE] = value?.toString()?.toItemNode()
            }

        abstract fun build(): SyncObject
    }

    enum class CollectionDataType(override val value: String) : SyncObjectEnum {
        @Json(name = "KWCollection")
        KWCOLLECTION("KWCollection"),

        @Json(name = "KWIdentity")
        KWIDENTITY("KWIdentity"),

        @Json(name = "KWPaymentMean_creditCard")
        KWPAYMENTMEAN_CREDITCARD("KWPaymentMean_creditCard"),

        @Json(name = "KWCompany")
        KWCOMPANY("KWCompany"),

        @Json(name = "KWPhone")
        KWPHONE("KWPhone"),

        @Json(name = "KWSecureNote")
        KWSECURENOTE("KWSecureNote"),

        @Json(name = "KWAddress")
        KWADDRESS("KWAddress"),

        @Json(name = "KWPersonalWebsite")
        KWPERSONALWEBSITE("KWPersonalWebsite"),

        @Json(name = "KWFiscalStatement")
        KWFISCALSTATEMENT("KWFiscalStatement"),

        @Json(name = "KWAuthentifiant")
        KWAUTHENTIFIANT("KWAuthentifiant"),

        @Json(name = "KWIDCard")
        KWIDCARD("KWIDCard"),

        @Json(name = "KWBankStatement")
        KWBANKSTATEMENT("KWBankStatement"),

        @Json(name = "KWEmail")
        KWEMAIL("KWEmail"),

        @Json(name = "KWPassport")
        KWPASSPORT("KWPassport"),

        @Json(name = "KWSocialSecurityStatement")
        KWSOCIALSECURITYSTATEMENT("KWSocialSecurityStatement"),

        @Json(name = "KWDriverLicence")
        KWDRIVERLICENCE("KWDriverLicence"),
    }

    enum class Platform(override val value: String) : SyncObjectEnum {
        @Json(name = "server_iphone")
        SERVER_IPHONE("server_iphone"),

        @Json(name = "server_safari")
        SERVER_SAFARI("server_safari"),

        @Json(name = "server_ipad")
        SERVER_IPAD("server_ipad"),

        @Json(name = "website")
        WEBSITE("website"),

        @Json(name = "server_standalone")
        SERVER_STANDALONE("server_standalone"),

        @Json(name = "server_carbon_tests")
        SERVER_CARBON_TESTS("server_carbon_tests"),

        @Json(name = "desktop_win")
        DESKTOP_WIN("desktop_win"),

        @Json(name = "real_website")
        REAL_WEBSITE("real_website"),

        @Json(name = "server_carbon_simulator")
        SERVER_CARBON_SIMULATOR("server_carbon_simulator"),

        @Json(name = "server_win")
        SERVER_WIN("server_win"),

        @Json(name = "server_leeloo_dev")
        SERVER_LEELOO_DEV("server_leeloo_dev"),

        @Json(name = "server_carbon_unknown")
        SERVER_CARBON_UNKNOWN("server_carbon_unknown"),

        @Json(name = "server_wac")
        SERVER_WAC("server_wac"),

        @Json(name = "server_tac")
        SERVER_TAC("server_tac"),

        @Json(name = "server_android")
        SERVER_ANDROID("server_android"),

        @Json(name = "server_catalyst")
        SERVER_CATALYST("server_catalyst"),

        @Json(name = "server_ipod")
        SERVER_IPOD("server_ipod"),

        @Json(name = "server_macosx")
        SERVER_MACOSX("server_macosx"),

        @Json(name = "server_leeloo")
        SERVER_LEELOO("server_leeloo"),
    }

    enum class Gender(override val value: String) : SyncObjectEnum {
        @Json(name = "MALE")
        MALE("MALE"),

        @Json(name = "FEMALE")
        FEMALE("FEMALE"),
    }

    enum class SecureNoteType(override val value: String) : SyncObjectEnum {
        @Json(name = "EARTH")
        EARTH("EARTH"),

        @Json(name = "GRAY")
        GRAY("GRAY"),

        @Json(name = "PINK")
        PINK("PINK"),

        @Json(name = "BLUE")
        BLUE("BLUE"),

        @Json(name = "CRIMSON")
        CRIMSON("CRIMSON"),

        @Json(name = "PURPLE")
        PURPLE("PURPLE"),

        @Json(name = "YELLOW")
        YELLOW("YELLOW"),

        @Json(name = "GREEN")
        GREEN("GREEN"),

        @Json(name = "NO_TYPE")
        NO_TYPE("NO_TYPE"),

        @Json(name = "ORANGE")
        ORANGE("ORANGE"),
    }

    enum class WiFiEncryptionType(override val value: String) : SyncObjectEnum {
        @Json(name = "wpa-personal")
        WPA_PERSONAL("wpa-personal"),

        @Json(name = "wpa2-personal")
        WPA2_PERSONAL("wpa2-personal"),

        @Json(name = "wep")
        WEP("wep"),

        @Json(name = "wpa3-personal")
        WPA3_PERSONAL("wpa3-personal"),

        @Json(name = "unsecured")
        UNSECURED("unsecured"),
    }

    enum class AutofillDisabledSourceType(override val value: String) : SyncObjectEnum {
        @Json(name = "KWIdentity")
        KWIDENTITY("KWIdentity"),

        @Json(name = "KWPaymentMean_creditCard")
        KWPAYMENTMEAN_CREDITCARD("KWPaymentMean_creditCard"),

        @Json(name = "KWCompany")
        KWCOMPANY("KWCompany"),

        @Json(name = "KWPhone")
        KWPHONE("KWPhone"),

        @Json(name = "KWMerchand")
        KWMERCHAND("KWMerchand"),

        @Json(name = "KWSecureNote")
        KWSECURENOTE("KWSecureNote"),

        @Json(name = "KWAddress")
        KWADDRESS("KWAddress"),

        @Json(name = "KWPersonalWebsite")
        KWPERSONALWEBSITE("KWPersonalWebsite"),

        @Json(name = "KWSecret")
        KWSECRET("KWSecret"),

        @Json(name = "KWFiscalStatement")
        KWFISCALSTATEMENT("KWFiscalStatement"),

        @Json(name = "KWWiFi")
        KWWIFI("KWWiFi"),

        @Json(name = "KWAuthentifiant")
        KWAUTHENTIFIANT("KWAuthentifiant"),

        @Json(name = "KWIDCard")
        KWIDCARD("KWIDCard"),

        @Json(name = "KWPaymentMean_paypal")
        KWPAYMENTMEAN_PAYPAL("KWPaymentMean_paypal"),

        @Json(name = "KWPasskey")
        KWPASSKEY("KWPasskey"),

        @Json(name = "KWBankStatement")
        KWBANKSTATEMENT("KWBankStatement"),

        @Json(name = "KWEmail")
        KWEMAIL("KWEmail"),

        @Json(name = "KWPassport")
        KWPASSPORT("KWPassport"),

        @Json(name = "KWSocialSecurityStatement")
        KWSOCIALSECURITYSTATEMENT("KWSocialSecurityStatement"),

        @Json(name = "KWDriverLicence")
        KWDRIVERLICENCE("KWDriverLicence"),
    }
}