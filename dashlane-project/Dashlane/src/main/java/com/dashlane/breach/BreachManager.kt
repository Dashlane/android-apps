package com.dashlane.breach

import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.dashlane.announcements.modules.BreachAlertPopupModule
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.events.AppEvents
import com.dashlane.events.BreachStatusChangedEvent
import com.dashlane.events.BreachesRefreshedEvent
import com.dashlane.events.DarkWebSetupCompleteEvent
import com.dashlane.events.PasswordChangedEvent
import com.dashlane.events.register
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.events.user.ReceiveSecurityAlert
import com.dashlane.notificationcenter.alerts.BreachDataHelper
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.getAlertTypeForLogs
import com.dashlane.security.getItemTypesForLogs
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.createSecurityBreach
import com.dashlane.vault.model.leakedPasswordsSet
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreachManager @Inject constructor(
    private val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val darkWebMonitoringManager: DarkWebMonitoringManager,
    private val appEvents: AppEvents,
    private val dataSaver: DataSaver,
    private val credentialDataQuery: CredentialDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val breachAlertCenter: BreachAlertPopupModule,
    private val breachLoader: BreachLoader,
    private val breachDataHelper: BreachDataHelper,
    private val hermesLogRepository: LogRepository,
    private val breachService: BreachService,
) {

    private var lastRefresh = 0L
    private var refreshInProgress = false
    private var lastSession: String? = null

    private val sessionCoroutineScope: CoroutineScope?
        get() = sessionCoroutineScopeProvider.get()

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    init {
        appEvents.register<DarkWebSetupCompleteEvent>(this) {
            darkWebMonitoringManager.invalidateCache()
            refreshIfNecessary(true)
        }
        appEvents.register<PasswordChangedEvent>(this) {
            refreshIfNecessary(true)
            markBreachSolved()
        }
    }

    private fun markBreachSolved() {
        sessionCoroutineScope?.launch(mainCoroutineDispatcher) {
            breachLoader.getBreachesWrapper()
                .filter { !it.localBreach.solved && it.linkedAuthentifiant.isEmpty() && !it.publicBreach.hasCreditCardLeaked() }
                .forEach {
                    breachDataHelper.saveAndRemove(it, SyncObject.SecurityBreach.Status.SOLVED)
                    appEvents.post(BreachStatusChangedEvent())
                }
        }
    }

    fun refreshIfNecessary(forceRefresh: Boolean = false) {
        val session = sessionManager.session ?: return 
        invalidateCacheIfSessionChanged(session)

        if ((refreshInProgress || !needRefresh()) && !forceRefresh) {
            return
        }

        refreshInProgress = true

        val lastFetchedPublicRevision = lastFetchedPublicBreachRevision()
        val lastFetchedDarkWebDate = lastFetchedDarkWebDate()

        sessionCoroutineScope?.launch(mainCoroutineDispatcher) {
            val publicBreachResult = try {
                breachService.getBreaches(
                    fromRevision = lastFetchedPublicRevision,
                    
                    revisionOnly = lastFetchedPublicRevision == 0
                )
            } catch (e: Throwable) {
                refreshInProgress = false
                null
            } ?: return@launch

            val publicBreaches = publicBreachResult.breaches ?: listOf()

            val (newDarkWebDate, darkWebBreaches) = darkWebMonitoringManager.getBreaches(lastFetchedDarkWebDate)
                ?: Pair(0L, listOf())

            setRefreshed()

            val allBreaches = publicBreaches + darkWebBreaches

            if (allBreaches.isNotEmpty()) {
                withContext(defaultCoroutineDispatcher) { saveOrUpdateBreaches(allBreaches) }
            }
            saveLastFetchedPublicBreachRevision(publicBreachResult.currentRevision)
            if (newDarkWebDate > 0) {
                saveLastFetchedDarkWebDate(newDarkWebDate)
            }
            appEvents.post(BreachesRefreshedEvent())
            breachAlertCenter.onBreachUpdated()
            refreshInProgress = false
        }
    }

    @WorkerThread
    fun getSecurityBreachesToSave(breaches: List<BreachWithOriginalJson>):
            List<VaultItem<SyncObject.SecurityBreach>> {
        val existingSecurityBreaches = getAllSecurityBreaches()

        return breaches.mapNotNull { breachWithJson ->
            getSecurityBreachToSave(existingSecurityBreaches, breachWithJson)?.also {
                val breach = breachWithJson.breach
                hermesLogRepository.queueEvent(
                    ReceiveSecurityAlert(
                        securityAlertItemId = ItemId(breach.id),
                        itemTypesAffected = breach.getItemTypesForLogs(),
                        securityAlertType = breach.getAlertTypeForLogs()
                    )
                )
                breachWithJson.breach.isDarkWebBreach()
            }
        }
    }

    fun onTerminate() {
        breachAlertCenter.onTerminate()
    }

    @WorkerThread
    private suspend fun saveOrUpdateBreaches(breaches: List<BreachWithOriginalJson>) {
        val securityBreachesToSave = getSecurityBreachesToSave(breaches)

        saveSecurityBreaches(securityBreachesToSave)
    }

    private fun invalidateCacheIfSessionChanged(session: Session) {
        val sessionId = session.sessionId
        if (sessionId == lastSession) {
            return 
        }
        lastRefresh = 0 
        lastSession = sessionId
    }

    @WorkerThread
    @VisibleForTesting
    fun getSecurityBreachToSave(
        existingSecurityBreaches: Map<String, VaultItem<SyncObject.SecurityBreach>>,
        breachWithJson: BreachWithOriginalJson
    ): VaultItem<SyncObject.SecurityBreach>? {
        val breach = breachWithJson.breach
        val json = breachWithJson.json
        val passwordsInBreach = breachWithJson.passwords?.toSet() ?: setOf()

        val upcomingBreachId = breach.id
        val upcomingBreachRevision = breach.lastModificationRevision

        if (!breach.shouldBeDisplay()) {
            
            return null
        }

        val credentialsForBreach by lazy { getCredentialsForBreach(breach) }

        val affectedPasswords = breach.takeIf { it.hasLeakedData(Breach.DATA_PASSWORD) }
            ?.let { getAffectedPasswords(breach, credentialsForBreach) + passwordsInBreach }
            ?: passwordsInBreach

        
        
        
        val hasNoPrivateInformationOrCredential =
            !breach.hasPrivateInformationLeaked() || credentialsForBreach.isEmpty()
        if (affectedPasswords.isEmpty() && !breach.isDarkWebBreach() && hasNoPrivateInformationOrCredential) {
            return null
        }

        val existingSecurityBreach = existingSecurityBreaches[upcomingBreachId]

        
        existingSecurityBreach?.run {
            
            if (this.syncObject.content == json) return null

            val lLeakedPasswordsSet = syncObject.leakedPasswordsSet
            val updatedList = lLeakedPasswordsSet.plus(affectedPasswords)
            val (newLeakedPasswords, newStatus) = if (updatedList != lLeakedPasswordsSet) {
                updatedList to SyncObject.SecurityBreach.Status.PENDING
            } else {
                lLeakedPasswordsSet to syncObject.status
            }
            return this.copySyncObject {
                leakedPasswordsSet = newLeakedPasswords
                status = newStatus
                content = json
                contentRevision = upcomingBreachRevision.toLong()
            }.copyWithAttrs {
                syncState = if (breach.shouldBeDisplay()) {
                    SyncState.MODIFIED
                } else {
                    SyncState.DELETED
                }
            }
        }

        
        return createSecurityBreach(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                syncState = SyncState.MODIFIED,
                creationDate = Instant.now()
            ),
            breachId = upcomingBreachId,
            status = SyncObject.SecurityBreach.Status.PENDING,
            content = json,
            contentRevision = upcomingBreachRevision,
            leakedPasswords = affectedPasswords
        )
    }

    private fun setRefreshed() {
        lastRefresh = System.currentTimeMillis()
    }

    private fun needRefresh(): Boolean {
        
        return System.currentTimeMillis() - lastRefresh > DateUtils.DAY_IN_MILLIS
    }

    private fun lastFetchedPublicBreachRevision(): Int {
        return userPreferencesManager.getInt(PREF_LAST_BREACH_REVISION)
    }

    private fun saveLastFetchedPublicBreachRevision(revision: Int) {
        userPreferencesManager.putInt(PREF_LAST_BREACH_REVISION, revision)
    }

    private fun lastFetchedDarkWebDate(): Long {
        return userPreferencesManager.getLong(PREF_LAST_UPDATE_DARK_WEB)
    }

    private fun saveLastFetchedDarkWebDate(date: Long) {
        userPreferencesManager.putLong(PREF_LAST_UPDATE_DARK_WEB, date)
    }

    @WorkerThread
    private suspend fun saveSecurityBreaches(securityBreaches: List<VaultItem<SyncObject.SecurityBreach>>) {
        if (securityBreaches.isEmpty()) {
            return
        }
        securityBreaches.forEach { dataSaver.save(it) }
    }

    @Suppress("UNCHECKED_CAST")
    @WorkerThread
    private fun getAllSecurityBreaches(): Map<String, VaultItem<SyncObject.SecurityBreach>> {
        val filter = vaultFilter {
            
            ignoreUserLock()
            specificDataType(SyncObjectType.SECURITY_BREACH)
        }
        return vaultDataQuery.queryAllLegacy(filter).filter { it.syncObject is SyncObject.SecurityBreach }
            .map { it as VaultItem<SyncObject.SecurityBreach> }
            .associateBy { it.syncObject.breachId ?: "" }
    }

    private fun getAffectedPasswords(
        breach: Breach,
        credentialsForBreach: List<SummaryObject.Authentifiant>
    ): Set<String> {
        val eventDate = breach.eventDateParsed

        val ids = credentialsForBreach.mapNotNull { authentifiant ->
            val modificationDate = authentifiant.modificationDatetime
                ?: authentifiant.userModificationDatetime
                ?: authentifiant.creationDatetime
            if (modificationDate == null || modificationDate < eventDate) {
                authentifiant.id
            } else {
                null
            }
        }

        return vaultDataQuery.queryAllLegacy(
            vaultFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.AUTHENTIFIANT)
                specificUid(ids)
            }
        )
            .asSequence()
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .mapNotNull { it.syncObject.password }
            .distinct()
            .map { it.toString() }
            .toSet()
    }

    @WorkerThread
    private fun getCredentialsForBreach(breach: Breach): List<SummaryObject.Authentifiant> {
        val domains = breach.domains ?: return listOf()
        val filter = credentialDataQuery.createFilter().apply {
            
            ignoreUserLock()
            forDomains(domains)
        }
        return credentialDataQuery.queryAll(filter)
    }

    companion object {
        private const val PREF_LAST_BREACH_REVISION = "_pref_last_breach_revision"
        private const val PREF_LAST_UPDATE_DARK_WEB = "_pref_last_update_dark_web"
    }
}
