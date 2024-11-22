package com.dashlane.vault.util

import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject

fun VaultItem<SyncObject.Authentifiant>.isNotIncorrectApplicationSignatureWith(
    packageNameSignatureHelper: PackageNameSignatureHelper,
    packageName: String
): Boolean {
    return packageNameSignatureHelper.getPackageNameSignatureVerificationStatus(
        packageName,
        this.toSummary<SummaryObject.Authentifiant>().linkedServices,
        urlForUI()
    ) != PackageSignatureStatus.INCORRECT
}

fun SummaryObject.Authentifiant.isNotIncorrectApplicationSignatureWith(
    packageNameSignatureHelper: PackageNameSignatureHelper,
    packageName: String
): Boolean {
    return packageNameSignatureHelper.getPackageNameSignatureVerificationStatus(
        packageName,
        linkedServices,
        urlForUI()
    ) != PackageSignatureStatus.INCORRECT
}

fun SummaryObject.Authentifiant.getSignatureVerificationWith(
    packageNameSignatureHelper: PackageNameSignatureHelper,
    packageName: String
): PackageSignatureStatus {
    return packageNameSignatureHelper.getPackageNameSignatureVerificationStatus(packageName, linkedServices, urlForUI())
}

fun SummaryObject.Authentifiant.getDetailedSignatureVerificationWith(
    packageNameSignatureHelper: PackageNameSignatureHelper,
    packageName: String
): SignatureVerification {
    return packageNameSignatureHelper.getPackageNameDetailedSignatureVerificationStatus(
        packageName,
        linkedServices,
        urlForUI()
    )
}
