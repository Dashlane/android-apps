package com.dashlane.autofill.core.domain;

import android.content.Context;

import com.dashlane.applinkfetcher.AuthentifiantAppLinkDownloader;
import com.dashlane.autofill.AutofillAnalyzerDef;
import com.dashlane.core.helpers.SignatureVerification;
import com.dashlane.vault.summary.SummaryObject;
import com.dashlane.vault.util.AuthentifiantPackageNameSignatureUtilKt;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class AutofillSecurityApplication implements AutofillAnalyzerDef.IAutofillSecurityApplication {

    private final AuthentifiantAppLinkDownloader authentifiantAppLinkDownloader;

    @Inject
    public AutofillSecurityApplication(AuthentifiantAppLinkDownloader authentifiantAppLinkDownloader) {
        this.authentifiantAppLinkDownloader = authentifiantAppLinkDownloader;
    }

    @NotNull
    @Override
    public SignatureVerification getSignatureVerification(@NotNull Context context, @NotNull String packageName,
            @NotNull SummaryObject.Authentifiant authentifiant) {
        SignatureVerification signatureVerification = AuthentifiantPackageNameSignatureUtilKt
                .getDetailedSignatureVerificationWith(authentifiant, context, packageName);

        if (signatureVerification.isUnknown()) {
            authentifiantAppLinkDownloader.fetch(authentifiant);
        }

        return signatureVerification;
    }
}
