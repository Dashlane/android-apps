package com.dashlane.vault.model

import androidx.annotation.CheckResult
import com.dashlane.url.name
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.isUrlFormat
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import java.text.Normalizer
import java.time.Instant

@SuppressWarnings("kotlin:S107")
fun createAuthentifiant(
    itemId: String = generateUniqueIdentifier(),
    spaceId: String? = null,
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(uid = itemId, teamSpaceId = spaceId),
    title: String? = null,
    deprecatedUrl: String? = null,
    userSelectedUrl: String? = null,
    useFixedUrl: Boolean = false,
    email: String? = "",
    login: String? = "",
    password: SyncObfuscatedValue? = null,
    otpSecret: SyncObfuscatedValue? = null,
    otpUrl: SyncObfuscatedValue? = null,
    authExtra: String? = "",
    category: String? = "",
    note: String? = "",
    autoLogin: String? = "false",
    numberUses: String? = "",
    lastUse: String? = "",
    strength: String? = "",
    authMeta: SyncObject.Authentifiant.AppMetaData? = null,
    passwordModificationDate: Instant? = null,
    isChecked: Boolean = false,
    linkedServices: SyncObject.Authentifiant.LinkedServices? = null,
    isFavorite: Boolean = false
): VaultItem<SyncObject.Authentifiant> {
    return dataIdentifier.toVaultItem(
        SyncObject.Authentifiant {
            this.title = title
            this.url = deprecatedUrl
            this.userSelectedUrl = userSelectedUrl
            this.useFixedUrl = useFixedUrl
            this.email = email
            this.login = login
            this.password = password
            this.otpSecret = otpSecret
            this.otpUrl = otpUrl
            this.secondaryLogin = authExtra ?: ""
            this.category = category
            this.note = note
            this.autoLogin = autoLogin?.toBoolean()
            this.numberUse = numberUses?.toLongOrNull()
            this.lastUse = lastUse?.toLongOrNull()?.let { Instant.ofEpochSecond(it) }
            this.strength = strength?.toLongOrNull()
            this.appMetaData = authMeta
            this.modificationDatetime = passwordModificationDate
            this.checked = isChecked
            this.linkedServices = linkedServices
            this.isFavorite = isFavorite
            this.spaceId = spaceId

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

@CheckResult
fun SyncObject.Authentifiant.Companion.formatTitle(title: String?): String? =
    title?.toUrlOrNull()?.name ?: title

@CheckResult
fun SyncObject.Authentifiant.Companion.getDefaultName(value: String?): String {
    return if (value.isSemanticallyNull()) {
        ""
    } else {
        value!!.toUrlOrNull()?.root?.takeIf { it.isUrlFormat() } ?: value
    }
}

val VaultItem<SyncObject.Authentifiant>.loginForUi
    get() = toSummary<SummaryObject.Authentifiant>().loginForUi

val SummaryObject.Authentifiant.loginForUi
    get() = getLoginForUi(email = email, login = login)

private fun getLoginForUi(email: String?, login: String?): String? =
        login.takeIf { it.isNotSemanticallyNull() } ?: email.takeIf { it.isNotSemanticallyNull() }

val SummaryObject.Authentifiant.urlDomain: String?
    get() = urlForGoToWebsite?.toUrlOrNull()?.root

val VaultItem<SyncObject.Authentifiant>.urlDomain: String?
    get() = urlForGoToWebsite?.toUrlOrNull()?.root

val VaultItem<SyncObject.Authentifiant>.urlForUsageLog: String
    get() = toSummary<SummaryObject.Authentifiant>().urlForUsageLog

val SummaryObject.Authentifiant.urlForUsageLog: String
    get() {
        val url = urlForGoToWebsite
        return if (url == null) {
            linkedServices?.associatedAndroidApps?.map { it.packageName }?.firstOrNull()
                ?: "client__no_url_no_app"
        } else {
            url.toUrlOrNull()?.root ?: "client__not_valid_url"
        }
    }

val VaultItem<SyncObject.Authentifiant>.urlForGoToWebsite: String?
    get() = toSummary<SummaryObject.Authentifiant>().urlForGoToWebsite

val SummaryObject.Authentifiant.urlForGoToWebsite: String?
    get() {
        val userSelectedUrl = userSelectedUrl
        val trustedUrl = url
        val targetUrl = if (useFixedUrl == true && userSelectedUrl.isNotSemanticallyNull()) {
            userSelectedUrl
        } else {
            trustedUrl
        }

        return targetUrl?.toUrlOrNull(defaultSchemeHttp = true)?.toString()
    }

val String.getUrlDisplayName: String?
    get() = toUrlOrNull(false)?.root

fun SummaryObject.Authentifiant.urlForUI(): String? = if (useFixedUrl == true && userSelectedUrl.isNotSemanticallyNull()) {
    userSelectedUrl
} else {
    url
}

fun VaultItem<SyncObject.Authentifiant>.urlForUI(): String? =
    toSummary<SummaryObject.Authentifiant>().urlForUI()

val VaultItem<SyncObject.Authentifiant>.urls: Array<String?>
    get() = arrayOf(syncObject.url, syncObject.userSelectedUrl)

val VaultItem<SyncObject.Authentifiant>.navigationUrl: String?
    get() = toSummary<SummaryObject.Authentifiant>().urlForGoToWebsite

val SummaryObject.Authentifiant.navigationUrl: String?
    get() = urlForGoToWebsite

val SummaryObject.Authentifiant.titleForListNormalized: String?
    get() = titleForList?.let { Normalizer.normalize(it, Normalizer.Form.NFD) }

val VaultItem<SyncObject.Authentifiant>.titleForListNormalized: String?
    get() = toSummary<SummaryObject.Authentifiant>().titleForListNormalized

val SummaryObject.Authentifiant.titleForList: String?
    get() = title?.removeSurrounding("\"")?.takeIf { it.isNotSemanticallyNull() }
        ?: urlForUI()?.toUrlOrNull()?.host?.takeIf { it.isNotSemanticallyNull() }

fun VaultItem<SyncObject.Authentifiant>.copySyncObject(builder: SyncObject.Authentifiant.Builder.() -> Unit = {}):
    VaultItem<SyncObject.Authentifiant> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}

fun SummaryObject.LinkedServices?.getAllLinkedPackageName(): Set<String> {
    return this?.associatedAndroidApps?.mapNotNull { it.packageName }?.toSet() ?: setOf()
}
