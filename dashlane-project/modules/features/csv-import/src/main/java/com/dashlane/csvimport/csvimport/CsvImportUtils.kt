package com.dashlane.csvimport.csvimport

import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.helpers.AppSignature
import com.dashlane.core.helpers.toAppSignature
import com.dashlane.core.helpers.toSyncObject
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.isValidEmail
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import okio.ByteString.Companion.decodeBase64

internal fun Iterable<CsvAuthentifiant>.filterNew(
    existing: List<SyncObject.Authentifiant>
): List<CsvAuthentifiant> =
    (this.associateBy { it.toIdentifier() } - existing.map { it.toIdentifier() })
        .values
        .toList()

private fun SyncObject.Authentifiant.toIdentifier(): AuthentifiantIdentifier = AuthentifiantIdentifier(
    url = url,
    username = email.takeUnless { it.isSemanticallyNull() }
        ?: login.takeUnless { it.isSemanticallyNull() },
    password = password?.toString()
)

private fun CsvAuthentifiant.toIdentifier(): AuthentifiantIdentifier = AuthentifiantIdentifier(
    url = deprecatedUrl,
    username = email ?: login,
    password = password
)

private data class AuthentifiantIdentifier(
    private val url: String?,
    private val username: String?,
    private val password: String?
)

internal fun newCsvAuthentifiant(
    linkedServicesHelper: LinkedServicesHelper,
    knownApplicationProvider: KnownApplicationProvider,
    fields: List<String>,
    types: List<CsvSchema.FieldType?>,
    appNameFromPackage: (String) -> String?
): CsvAuthentifiant? {
    var url: String? = null
    var username: String? = null
    var password: String? = null

    for (i in fields.indices) {
        val field = fields[i].takeUnless { it.isBlank() }
            ?: continue

        when (types[i]) {
            CsvSchema.FieldType.URL -> url = field
            CsvSchema.FieldType.USERNAME -> username = field
            CsvSchema.FieldType.PASSWORD -> password = field
            else -> {}
        }
    }

    if (url == null || username == null || password == null) return null

    return if (url.startsWith("android://")) {
        newAuthentifiantForApp(
            linkedServicesHelper,
            knownApplicationProvider,
            url,
            username,
            password,
            appNameFromPackage
        )
    } else {
        newAuthentifiantForWebsite(url, username, password)
    }
}

private fun newAuthentifiantForApp(
    linkedServicesHelper: LinkedServicesHelper,
    knownApplicationProvider: KnownApplicationProvider,
    url: String,
    username: String,
    password: String,
    appNameFromPackage: (String) -> String?
): CsvAuthentifiant {
    val (signature, packageName) = url.drop(10) 
        .dropLast(1) 
        .split("@")
    val sha512Signature = signature.decodeBase64()?.hex()
    val linkedServices = sha512Signature?.let {
        val appSignatures = listOf(AppSignature(packageName, sha512Signatures = listOf(it)))
        linkedServicesHelper.addSignatureToLinkedServices(appSignatures, null)
    }

    val website = knownApplicationProvider.getKnownApplication(packageName)?.mainDomain

    return CsvAuthentifiant(
        appSignatures = linkedServices?.toAppSignature(),
        deprecatedUrl = website,
        login = username,
        password = password,
        title = website ?: appNameFromPackage(packageName)
    )
}

private fun newAuthentifiantForWebsite(
    url: String,
    username: String,
    password: String
): CsvAuthentifiant {
    val (login, email) = if (username.isValidEmail()) {
        null to username
    } else {
        username to null
    }

    return CsvAuthentifiant(
        deprecatedUrl = url,
        email = email,
        login = login,
        password = password
    )
}

internal fun CsvAuthentifiant.toVaultItem(helper: ImportAuthentifiantHelper): VaultItem<SyncObject.Authentifiant> =
    helper.newAuthentifiant(
        linkedServices = appSignatures?.toSyncObject(),
        deprecatedUrl = deprecatedUrl,
        email = email,
        login = login,
        password = SyncObfuscatedValue(password ?: ""),
        title = title
    )