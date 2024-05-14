package com.dashlane.vault

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.pause.services.PausedFormSourcesProvider
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ItemTypeCounts
import com.dashlane.hermes.generated.definitions.Scope
import com.dashlane.hermes.generated.events.user.VaultReport
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.security.identitydashboard.password.GroupOfAuthentifiant
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.counterFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.activities.fragments.vault.Filter.FILTER_ID
import com.dashlane.ui.activities.fragments.vault.Filter.FILTER_PASSWORD
import com.dashlane.ui.activities.fragments.vault.Filter.FILTER_PAYMENT
import com.dashlane.ui.activities.fragments.vault.Filter.FILTER_PERSONAL_INFO
import com.dashlane.ui.activities.fragments.vault.Filter.FILTER_SECURE_NOTE
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.obfuscated.isNullOrEmpty
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class VaultReportLogger @Inject constructor(
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
    private val appEvents: AppEvents,
    private val userPreferencesManager: UserPreferencesManager,
    private val logRepository: LogRepository,
    private val authentifiantSecurityEvaluator: AuthentifiantSecurityEvaluator,
    private val vaultDataQuery: VaultDataQuery,
    private val dataCounter: DataCounter,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val pausedFormSourcesProvider: PausedFormSourcesProvider,
    private val breachLoader: BreachLoader,
    private val knownApplicationProvider: KnownApplicationProvider,
    private val collectionsReportProvider: CollectionsReportProvider
) {
    fun start() {
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            if (it.state != SyncFinishedEvent.State.SUCCESS) return@register

            val now = Instant.now()
            if (now >= userPreferencesManager.vaultReportLatestTriggerTimestamp.plus(
                    24,
                    ChronoUnit.HOURS
                )
            ) {
                userPreferencesManager.vaultReportLatestTriggerTimestamp = now
                logVaultReports()
            }
        }
    }

    fun stop() {
        appEvents.unregister(this, SyncFinishedEvent::class.java)
    }

    private fun logVaultReports() {
        coroutineScope.launch {
            Scope.values().mapNotNull { buildVaultReport(it) }
                .forEach { logRepository.queueEvent(it) }
        }
    }

    @Suppress("LongMethod")
    private suspend fun buildVaultReport(scope: Scope): VaultReport? =
        withContext(Dispatchers.Default) {
            val teamspace = teamSpaceAccessorProvider.get()
                ?.availableSpaces
                ?.firstOrNull {
                    when (scope) {
                        Scope.GLOBAL -> it is TeamSpace.Combined
                        Scope.PERSONAL -> it is TeamSpace.Personal
                        Scope.TEAM -> it is TeamSpace.Business
                    }
                }
                ?: return@withContext null

            val credentials = vaultDataQuery.queryAll(
                vaultFilter {
                    ignoreUserLock()
                    specificDataType(FILTER_PASSWORD.syncObjectTypes)
                    specificSpace(teamspace)
                }
            )
            val totalSecureNotes = dataCounter.count(
                counterFilter {
                    ignoreUserLock()
                    specificDataType(FILTER_SECURE_NOTE.syncObjectTypes)
                    specificSpace(teamspace)
                }
            )
            val totalPayments = dataCounter.count(
                counterFilter {
                    ignoreUserLock()
                    specificDataType(FILTER_PAYMENT.syncObjectTypes)
                    specificSpace(teamspace)
                }
            )
            val totalPersonalInfo = dataCounter.count(
                counterFilter {
                    ignoreUserLock()
                    specificDataType(FILTER_PERSONAL_INFO.syncObjectTypes)
                    specificSpace(teamspace)
                }
            )
            val totalIds = dataCounter.count(
                counterFilter {
                    ignoreUserLock()
                    specificDataType(FILTER_ID.syncObjectTypes)
                    specificSpace(teamspace)
                }
            )

            val evaluatorResult = authentifiantSecurityEvaluator.computeResult(
                teamSpace = teamspace,
                ignoreUserLock = true
            )
            val securityAlerts = breachLoader.getBreachesWrapper(ignoreUserLock = true)
            val darkWebAlerts = securityAlerts.filter { it.publicBreach.isDarkWebBreach() }
            val authentifiantsByDarkWebBreach =
                evaluatorResult.authentifiantsByBreach.filter { it.groupBy.isDarkWebBreach() }

            val passwordReused = evaluatorResult.authentifiantsBySimilarity.uniqueAuthentifiant()
            val passwordCompromised = evaluatorResult.authentifiantsByBreach.uniqueAuthentifiant()
            val passwordsWeak = evaluatorResult.authentifiantsByStrength.uniqueAuthentifiant()
            VaultReport(
                scope = scope,
                passwordsTotalCount = evaluatorResult.totalCredentials,
                passwordsSafeCount = evaluatorResult.totalSafeCredentials,
                passwordsReusedCount = passwordReused.count(),
                passwordsCompromisedCount = passwordCompromised.count(),
                passwordsWeakCount = passwordsWeak.count(),
                passwordsExcludedCount = evaluatorResult.authentifiantsIgnored.count(),
                securityScore = evaluatorResult.securityScore?.let { score ->
                    (score.value.coerceIn(0f, 1f) * 100).roundToInt()
                },
                passwordsWithOtpCount = credentials.mapNotNull { it.syncObject as? SyncObject.Authentifiant }
                    .count { !it.password.isNullOrEmpty() && (!it.otpSecret.isNullOrEmpty() || !it.otpUrl.isNullOrEmpty()) },
                passwordChangerCompatibleCredentialsCount = 0,
                domainsWithoutAutofillCount = computeDomainsWithoutAutofillCount(),
                passwordsWithAutologinDisabledCount = computeDomainsWithoutAutofillCount(),
                securityAlertsCount = securityAlerts.count(),
                securityAlertsActiveCount = evaluatorResult.authentifiantsByBreach.count(),
                darkWebAlertsCount = darkWebAlerts.count(),
                darkWebAlertsActiveCount = authentifiantsByDarkWebBreach.count(),
                passwordsCompromisedThroughDarkWebCount = authentifiantsByDarkWebBreach.uniqueAuthentifiantCount(),
                payments = ItemTypeCounts(totalCount = totalPayments),
                personalInfo = ItemTypeCounts(totalCount = totalPersonalInfo),
                passwords = ItemTypeCounts(
                    totalCount = credentials.size,
                    collectionsCount = collectionsReportProvider.computeCollectionsWithLoginCount(teamspace),
                    singleCollectionCount = collectionsReportProvider.computeLoginsWithSingleCollectionCount(teamspace),
                    multipleCollectionsCount = collectionsReportProvider.computeLoginsWithMultipleCollectionsCount(
                        teamspace
                    ),
                ),
                secureNotes = ItemTypeCounts(totalCount = totalSecureNotes),
                ids = ItemTypeCounts(totalCount = totalIds),
                collectionsTotalCount = collectionsReportProvider.computeCollectionTotalCount(teamspace),
                itemsPerCollectionAverageCount = collectionsReportProvider.computeItemPerCollectionAverageCount(
                    teamspace
                ),
                collectionsPerItemAverageCount = collectionsReportProvider.computeCollectionPerItemAverageCount(
                    teamspace
                )
            )
        }

    private suspend fun computeDomainsWithoutAutofillCount(): Int {
        val now = Instant.now()

        return pausedFormSourcesProvider.getAllPausedFormSources()
            .filter { it.pauseUntil > now }
            .mapNotNull {
                when (val source = it.autoFillFormSource) {
                    is ApplicationFormSource -> knownApplicationProvider.getKnownApplication(source.packageName)?.mainDomain
                    is WebDomainFormSource -> source.webDomain
                }?.toUrlDomainOrNull()
            }
            .toSet()
            .count()
    }

    companion object {
        private fun List<GroupOfAuthentifiant<*>>.uniqueAuthentifiantCount() =
            flatMap { it.authentifiants }.toSet().count()

        private fun List<GroupOfAuthentifiant<*>>.uniqueAuthentifiant() =
            flatMap { it.authentifiants }.toSet()
    }
}
