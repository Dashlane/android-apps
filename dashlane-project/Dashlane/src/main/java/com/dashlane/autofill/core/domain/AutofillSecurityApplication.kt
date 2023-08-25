package com.dashlane.autofill.core.domain

import android.content.Context
import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader
import com.dashlane.autofill.AutofillAnalyzerDef.IAutofillSecurityApplication
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.getDetailedSignatureVerificationWith
import javax.inject.Inject

class AutofillSecurityApplication @Inject constructor(
    private val authentifiantAppLinkDownloader: AuthentifiantAppLinkDownloader,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) : IAutofillSecurityApplication {
    override fun getSignatureVerification(
        context: Context,
        packageName: String,
        authentifiant: SummaryObject.Authentifiant
    ): SignatureVerification {
        val signatureVerification =
            authentifiant.getDetailedSignatureVerificationWith(packageNameSignatureHelper, packageName)
        if (signatureVerification.isUnknown()) {
            authentifiantAppLinkDownloader.fetch(authentifiant)
        }
        return signatureVerification
    }
}