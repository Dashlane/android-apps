package com.dashlane.vault.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.PackageUtilities
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AuthentifiantPackageNameMatcher {
    fun matchPackageName(
        item: VaultItem<SyncObject.Authentifiant>,
        packageName: String
    ): Boolean = matchPackageName(item.toSummary(), packageName)

    fun matchPackageName(
        item: SummaryObject.Authentifiant,
        packageName: String
    ): Boolean
}

class AuthentifiantPackageNameMatcherImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) : AuthentifiantPackageNameMatcher {

    override fun matchPackageName(
        item: SummaryObject.Authentifiant,
        packageName: String
    ): Boolean {
        return (
            item.matchByPackageNameInVault(packageName) ||
                item.matchByPackageNameKeywords(context, packageName)
            ) &&
            item.isNotIncorrectApplicationSignatureWith(packageNameSignatureHelper, packageName)
    }

    private fun SummaryObject.Authentifiant.matchByPackageNameInVault(packageName: String) =
        linkedServices?.associatedAndroidApps?.any { it.packageName == packageName } ?: false

    private fun SummaryObject.Authentifiant.matchByPackageNameKeywords(
        context: Context,
        packageName: String
    ): Boolean {
        val keywords = PackageUtilities.getKeywords(context, packageName)
        return matchKeywords(keywords)
    }
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
