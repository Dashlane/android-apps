package com.dashlane.autofill.api.securitywarnings.model

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.url.UrlDomain

sealed class RememberSecurityWarning(
    open val signatureVerification: SignatureVerification,
    open val item: Item,
    open val source: Source
) {
    fun signaturesPackageNamesMatchesSources(): Boolean =
        signatureVerification.signatureInstalled?.packageName == source.packageName

    fun allSignatures(): List<String>? {
        return signatureVerification.signatureInstalled?.let {
            it.sha256Signatures.orEmpty() + it.sha512Signatures.orEmpty()
        }?.takeIf {
            it.isNotEmpty()
        }
    }

    class IncorrectAppSoftMatch(
        override val signatureVerification: SignatureVerification.Incorrect,
        override val item: Item.SoftMatchItem,
        override val source: Source.App
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class IncorrectAppUrlMatch(
        override val signatureVerification: SignatureVerification.Incorrect,
        override val item: Item.UrlMatchItem,
        override val source: Source.App
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class IncorrectPageSoftMatch(
        override val signatureVerification: SignatureVerification.Incorrect,
        override val item: Item.SoftMatchItem,
        override val source: Source.Page
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class IncorrectPageUrlMatch(
        override val signatureVerification: SignatureVerification.Incorrect,
        override val item: Item.UrlMatchItem,
        override val source: Source.Page
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class UnknownAppSoftMatch(
        override val signatureVerification: SignatureVerification.UnknownWithSignature,
        override val item: Item.SoftMatchItem,
        override val source: Source.App
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class UnknownAppUrlMatch(
        override val signatureVerification: SignatureVerification.UnknownWithSignature,
        override val item: Item.UrlMatchItem,
        override val source: Source.App
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class UnknownPageSoftMatch(
        override val signatureVerification: SignatureVerification.UnknownWithSignature,
        override val item: Item.SoftMatchItem,
        override val source: Source.Page
    ) : RememberSecurityWarning(signatureVerification, item, source)

    class UnknownPageUrlMatch(
        override val signatureVerification: SignatureVerification.UnknownWithSignature,
        override val item: Item.UrlMatchItem,
        override val source: Source.Page
    ) : RememberSecurityWarning(signatureVerification, item, source)
}

sealed class Item(val value: String) {
    class SoftMatchItem(authentifiantId: String) : Item(authentifiantId)

    class UrlMatchItem(urlDomain: UrlDomain) : Item(urlDomain.root.value)
}

sealed class Source(val packageName: String) {
    class App(applicationFormSource: ApplicationFormSource) : Source(applicationFormSource.packageName)

    class Page(webDomainFormSource: WebDomainFormSource) : Source(webDomainFormSource.packageName) {
        val url = webDomainFormSource.webDomain
    }
}
