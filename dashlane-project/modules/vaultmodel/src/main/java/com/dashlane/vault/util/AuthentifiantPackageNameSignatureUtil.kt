package com.dashlane.vault.util

import android.content.Context
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject



fun SyncObject.Authentifiant.isNotIncorrectApplicationSignatureWith(context: Context, packageName: String): Boolean {
    return PackageNameSignatureHelper()
        .getPackageNameSignatureVerificationStatus(
            context,
            packageName,
            this.toSummary<SummaryObject.Authentifiant>().linkedServices,
            urlForUI()
        ) != PackageSignatureStatus.INCORRECT
}

fun SummaryObject.Authentifiant.isNotIncorrectApplicationSignatureWith(context: Context, packageName: String): Boolean {
    return PackageNameSignatureHelper()
        .getPackageNameSignatureVerificationStatus(
            context,
            packageName,
            linkedServices,
            urlForUI()
        ) != PackageSignatureStatus.INCORRECT
}



fun SummaryObject.Authentifiant.getSignatureVerificationWith(
    context: Context,
    packageName: String
): PackageSignatureStatus {
    return PackageNameSignatureHelper()
        .getPackageNameSignatureVerificationStatus(context, packageName, linkedServices, urlForUI())
}



fun SummaryObject.Authentifiant.getDetailedSignatureVerificationWith(context: Context, packageName: String):
        SignatureVerification {
    return PackageNameSignatureHelper()
        .getPackageNameDetailedSignatureVerificationStatus(context, packageName, linkedServices, urlForUI())
}
