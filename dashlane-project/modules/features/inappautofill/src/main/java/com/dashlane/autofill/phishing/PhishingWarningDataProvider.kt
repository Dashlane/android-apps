package com.dashlane.autofill.phishing

import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.url.toUrlDomain
import com.dashlane.util.toRootUrlDomain
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUI
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

interface PhishingWarningDataProvider {
    fun shouldDisplayPhishingWarning(
        phishingAttemptLevel: PhishingAttemptLevel,
        summary: AutoFillHintSummary?,
        vaultItem: VaultItem<SyncObject.Authentifiant>?
    ): Boolean

    fun isWebsiteIgnored(website: String?): Boolean

    fun addPhishingWebsiteIgnored(website: String?)
}

class PhishingWarningDataProviderImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
) : PhishingWarningDataProvider {
    override fun shouldDisplayPhishingWarning(
        phishingAttemptLevel: PhishingAttemptLevel,
        summary: AutoFillHintSummary?,
        vaultItem: VaultItem<SyncObject.Authentifiant>?
    ): Boolean {
        val linkedWebsites = vaultItem?.syncObject?.linkedServices?.associatedDomains?.mapNotNull { it.domain?.toRootUrlDomain() }
        return phishingAttemptLevel != PhishingAttemptLevel.NONE &&
            vaultItem?.urlForUI()?.toRootUrlDomain() != summary?.webDomain?.toRootUrlDomain() &&
            linkedWebsites?.any { it == summary?.webDomain?.toRootUrlDomain() } != true &&
            !isWebsiteIgnored(summary?.webDomain)
    }

    override fun isWebsiteIgnored(website: String?): Boolean {
        return preferencesManager[sessionManager.session?.username].getPhishingWebsiteIgnored().any {
            it.toUrlDomain().root.value == website?.toUrlDomain()?.root?.value
        }
    }

    override fun addPhishingWebsiteIgnored(website: String?) {
        website?.let {
            preferencesManager[sessionManager.session?.username].addPhishingWebsiteIgnored(it)
        }
    }
}