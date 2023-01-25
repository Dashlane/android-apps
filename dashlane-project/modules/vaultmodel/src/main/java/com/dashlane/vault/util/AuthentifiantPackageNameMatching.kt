package com.dashlane.vault.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.PackageUtilities
import com.dashlane.vault.model.urls
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject



fun SyncObject.Authentifiant.matchPackageName(context: Context, packageName: String): Boolean {
    return (matchByPackageNameInVault(packageName) ||
            matchByPackageNameKeywords(context, packageName)) &&
            isNotIncorrectApplicationSignatureWith(context, packageName)
}

private fun SyncObject.Authentifiant.matchByPackageNameInVault(packageName: String) =
    linkedServices?.associatedAndroidApps?.any { it.packageName == packageName } ?: false

private fun SyncObject.Authentifiant.matchByPackageNameKeywords(context: Context, packageName: String): Boolean {
    val keywords = PackageUtilities.getKeywords(context, packageName)
    return matchKeywords(keywords)
}

@VisibleForTesting
fun SyncObject.Authentifiant.matchKeywords(keywords: Collection<String>): Boolean {
    return keywords.any { keyword ->
        title?.contains(keyword, ignoreCase = true) ?: false ||
                urls.any { it?.toUrlOrNull()?.host?.contains(keyword, ignoreCase = true) ?: false }
    }
}

fun SummaryObject.Authentifiant.matchPackageName(context: Context, packageName: String): Boolean {
    return (matchByPackageNameInVault(packageName) ||
            matchByPackageNameKeywords(context, packageName)) &&
            isNotIncorrectApplicationSignatureWith(context, packageName)
}

private fun SummaryObject.Authentifiant.matchByPackageNameInVault(packageName: String) =
    linkedServices?.associatedAndroidApps?.any { it.packageName == packageName } ?: false

private fun SummaryObject.Authentifiant.matchByPackageNameKeywords(context: Context, packageName: String): Boolean {
    val keywords = PackageUtilities.getKeywords(context, packageName)
    return matchKeywords(keywords)
}

@VisibleForTesting
fun SummaryObject.Authentifiant.matchKeywords(keywords: Collection<String>): Boolean {
    return keywords.any { keyword ->
        title?.contains(keyword, ignoreCase = true) ?: false ||
                arrayOf(url, userSelectedUrl).any {
                    it?.toUrlOrNull()?.host?.contains(keyword, ignoreCase = true) ?: false
                }
    }
}
