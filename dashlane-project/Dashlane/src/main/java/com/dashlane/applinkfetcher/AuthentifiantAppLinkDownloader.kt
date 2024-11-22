package com.dashlane.applinkfetcher

import android.app.Activity
import android.os.Bundle
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.helpers.AppSignature
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.credentialFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.url.UrlDomain
import com.dashlane.url.assetlinks.UrlDomainAssetLink
import com.dashlane.url.assetlinks.UrlDomainAssetLinkService
import com.dashlane.url.assetlinks.getAssetLinksOrNull
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.url.toUrlOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import okio.ByteString

@Singleton
class AuthentifiantAppLinkDownloader @Inject constructor(
    @ApplicationCoroutineScope
    applicationCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val assetLinkService: UrlDomainAssetLinkService,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val dataSaver: DataSaver,
    private val credentialDataQuery: CredentialDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val dataSync: DataSync,
    private val linkedServicesHelper: LinkedServicesHelper
) : AbstractActivityLifecycleListener() {

    private val clock = Clock.systemUTC()

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val actor = applicationCoroutineScope.actor<Fetch>(capacity = Channel.UNLIMITED) {
        
        val fetched = mutableSetOf<UrlDomain>()
        for (command in this) {
            when (command) {
                is Fetch.All -> {
                    refreshAll()
                    dataSync.sync()
                }
                is Fetch.Url -> {
                    val domain = command.domain
                    if (!fetched.contains(domain)) {
                        getAssetLinks(domain, listOf(command.url))
                        fetched += domain
                        dataSync.sync()
                    }
                }
            }
        }
    }

    sealed class Fetch {
        object All : Fetch()

        data class Url(
            val domain: UrlDomain,
            val url: String
        ) : Fetch()
    }

    fun fetch(authentifiant: SummaryObject.Authentifiant) {
        val url = authentifiant.urlForUI() ?: return
        val domain = url.toUrlDomainOrNull() ?: return
        runCatching { actor.trySend(Fetch.Url(domain, url)) }
    }

    private suspend fun refreshAll() {
        val preferences = preferencesManager[sessionManager.session?.username]
        val lastRefreshTime = Instant.ofEpochMilli(preferences.getLong(PREF_LAST_REFRESH_ALL))
        val instant = clock.instant()
        if (instant < lastRefreshTime + Duration.ofDays(7)) {
            return 
        }
        val urls = credentialDataQuery.queryAllUrls()
        val domains = urls.groupBy(String::toUrlDomainOrNull)
        domains.forEach { (domain, urls) ->
            if (domain != null) {
                getAssetLinks(domain, urls)
            }
        }
        preferences.putLong(PREF_LAST_REFRESH_ALL, instant.toEpochMilli())
    }

    private suspend fun getAssetLinks(domain: UrlDomain, urls: List<String>) {
        if (urls.isEmpty()) return
        val assetLinks = assetLinkService.getAssetLinksOrNull(domain) ?: return
        val appTargets =
            assetLinks.map(UrlDomainAssetLink::target).filterIsInstance<UrlDomainAssetLink.Target.AndroidApp>()
        if (appTargets.isEmpty()) return
        val appSignatures = appTargets.map(UrlDomainAssetLink.Target.AndroidApp::toAppSignature)
        withContext(defaultCoroutineDispatcher) { onLoadForUrls(urls, appSignatures) }
    }

    private suspend fun onLoadForUrls(urls: List<String>, result: List<AppSignature>) {
        val filter = credentialFilter { forDomains(urls.mapNotNull { it.toUrlOrNull()?.host }) }
        val summaryList = credentialDataQuery.queryAll(filter).map { it.id }

        val authentifiantFilter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(summaryList)
        }
        saveAppSignatures(authentifiantFilter, result)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun saveAppSignatures(
        authentifiantFilter: VaultFilter,
        result: List<AppSignature>
    ) {
        vaultDataQuery.queryAll(authentifiantFilter)
            .map { vaultItem ->
                vaultItem as VaultItem<SyncObject.Authentifiant>
                val newLinkedServices = linkedServicesHelper.addSignatureToLinkedServices(
                    result,
                    vaultItem.syncObject.linkedServices,
                    SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.DASHLANE
                )
                Pair(vaultItem, newLinkedServices)
            }
            .filter { (vaultItem, newLinkedServices) ->
                newLinkedServices != vaultItem.syncObject.linkedServices
            }
            .map { (vaultItem, newLinkedServices) ->
                vaultItem.copy(syncState = SyncState.MODIFIED).copySyncObject { linkedServices = newLinkedServices }
            }
            .let { dataSaver.save(DataSaver.SaveRequest(it)) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (activity is HomeActivity) {
            
            runCatching { actor.trySend(Fetch.All) }
        }
    }

    companion object {
        private const val PREF_LAST_REFRESH_ALL = "pref_last_refresh_all"
    }
}

private fun UrlDomainAssetLink.Target.AndroidApp.toAppSignature() = AppSignature(
    packageName,
    sha256signatures.map(ByteString::hex)
)
