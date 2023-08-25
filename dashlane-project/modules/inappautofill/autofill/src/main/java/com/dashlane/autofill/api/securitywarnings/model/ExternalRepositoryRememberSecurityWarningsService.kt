package com.dashlane.autofill.api.securitywarnings.model

import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class ExternalRepositoryRememberSecurityWarningsService @Inject constructor(
    private val repository: RememberSecurityWarningsRepository
) : RememberSecurityWarningsService {

    override fun remember(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        verification: SignatureVerification
    ): Boolean {
        val securityWarning = buildSecurityWarning(unlockedAuthentifiant, verification) ?: return false

        return repository.add(securityWarning)
    }

    private fun buildSecurityWarning(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        verification: SignatureVerification
    ): RememberSecurityWarning? {
        val item = buildItem(unlockedAuthentifiant.authentifiantSummary)
        val source = buildSource(unlockedAuthentifiant.formSource) ?: return null

        return when {
            verification is SignatureVerification.Incorrect && source is Source.App && item is Item.SoftMatchItem ->
                RememberSecurityWarning.IncorrectAppSoftMatch(verification, item, source)

            verification is SignatureVerification.Incorrect && source is Source.App && item is Item.UrlMatchItem ->
                RememberSecurityWarning.IncorrectAppUrlMatch(verification, item, source)

            verification is SignatureVerification.Incorrect && source is Source.Page && item is Item.SoftMatchItem ->
                RememberSecurityWarning.IncorrectPageSoftMatch(verification, item, source)

            verification is SignatureVerification.Incorrect && source is Source.Page && item is Item.UrlMatchItem ->
                RememberSecurityWarning.IncorrectPageUrlMatch(verification, item, source)

            verification is SignatureVerification.UnknownWithSignature && source is Source.App && item is Item.SoftMatchItem ->
                RememberSecurityWarning.UnknownAppSoftMatch(verification, item, source)

            verification is SignatureVerification.UnknownWithSignature && source is Source.App && item is Item.UrlMatchItem ->
                RememberSecurityWarning.UnknownAppUrlMatch(verification, item, source)

            verification is SignatureVerification.UnknownWithSignature && source is Source.Page && item is Item.SoftMatchItem ->
                RememberSecurityWarning.UnknownPageSoftMatch(verification, item, source)

            verification is SignatureVerification.UnknownWithSignature && source is Source.Page && item is Item.UrlMatchItem ->
                RememberSecurityWarning.UnknownPageUrlMatch(verification, item, source)

            else -> null
        }?.takeIf { it.signaturesPackageNamesMatchesSources() }
    }

    private fun buildItem(authentifiant: SummaryObject.Authentifiant): Item {
        return authentifiant.itemOrUrlDomainFlow(
            itemFlow = {
                Item.SoftMatchItem(it)
            },
            urlDomainFlow = {
                Item.UrlMatchItem(it)
            }
        )
    }

    private fun buildSource(formSource: AutoFillFormSource): Source? {
        return when (formSource) {
            is ApplicationFormSource -> Source.App(formSource)
            is WebDomainFormSource -> formSource.buildUrlDomainRootVersion()?.let { Source.Page(it) }
        }
    }

    private fun WebDomainFormSource.buildUrlDomainRootVersion(): WebDomainFormSource? {
        return this.webDomain.toUrlDomainOrNull()?.root?.let {
            WebDomainFormSource(this.packageName, it.value)
        }
    }

    override fun isItemSourceRemembered(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        verification: SignatureVerification
    ): Boolean {
        val securityWarning = buildSecurityWarning(unlockedAuthentifiant, verification) ?: return false

        return repository.has(securityWarning)
    }

    override fun isSourceRemembered(
        unlockedAuthentifiant: UnlockedAuthentifiant,
        verification: SignatureVerification
    ): Boolean {
        val securityWarning = buildSecurityWarning(unlockedAuthentifiant, verification) ?: return false

        return repository.hasSource(securityWarning)
    }

    override fun forgetAll() {
        repository.clearAll()
    }

    private fun SummaryObject.Authentifiant.itemOrUrlDomainFlow(
        itemFlow: (String) -> Item,
        urlDomainFlow: (UrlDomain) -> Item
    ): Item {
        return getUrlDomainRoot()?.let { urlDomainFlow.invoke(it) } ?: itemFlow.invoke(id)
    }

    private fun SummaryObject.Authentifiant.getUrlDomainRoot(): UrlDomain? {
        return if (
            this.urlForUsageLog == "client__no_url_no_app" ||
            this.urlForUsageLog == "client__not_valid_url" ||
            this.urlForUsageLog == ""
        ) {
            null
        } else {
            this.urlForUsageLog.toUrlDomainOrNull()?.root
        }
    }
}
