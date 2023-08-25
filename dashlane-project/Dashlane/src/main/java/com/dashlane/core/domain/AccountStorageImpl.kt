package com.dashlane.core.domain

import android.content.Context
import com.dashlane.core.DataSync
import com.dashlane.csvimport.ImportAuthentifiantHelper
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStorageImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val mainDataAccessor: MainDataAccessor,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val dataSync: DataSync,
    private val activityLogger: VaultActivityLogger
) : ImportAuthentifiantHelper, VpnThirdPartyAuthentifiantHelper {

    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()

    private val dataCounter: DataCounter
        get() = mainDataAccessor.getDataCounter()

    @Suppress("UNCHECKED_CAST")
    override fun newAuthentifiant(
        linkedServices: SyncObject.Authentifiant.LinkedServices?,
        deprecatedUrl: String?,
        email: String?,
        login: String?,
        password: SyncObfuscatedValue?,
        title: String?,
        teamId: String?
    ): VaultItem<SyncObject.Authentifiant> {
        val timestamp = Instant.now()
        return createAuthentifiant(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                teamSpaceId = teamId,
                syncState = SyncState.MODIFIED,
                creationDate = timestamp,
                userModificationDate = timestamp
            ),
            title = SyncObject.Authentifiant.formatTitle(title),
            deprecatedUrl = deprecatedUrl,
            email = email,
            login = login,
            password = password,
            autoLogin = "true",
            passwordModificationDate = timestamp,
            linkedServices = linkedServices
        ).copyWithDefaultValue(
            context,
            sessionManager.session
        ) as VaultItem<SyncObject.Authentifiant>
    }

    override fun newAuthentifiant(
        title: String,
        deprecatedUrl: String,
        email: String,
        password: SyncObfuscatedValue
    ): VaultItem<SyncObject.Authentifiant> = createAuthentifiant(
        title = title,
        deprecatedUrl = deprecatedUrl,
        email = email,
        password = password
    )

    override suspend fun addAuthentifiant(authentifiant: VaultItem<SyncObject.Authentifiant>) {
        addAuthentifiants(listOf(authentifiant), UsageLogCode11.From.MANUAL)
    }

    override suspend fun addAuthentifiants(
        authentifiants: List<VaultItem<SyncObject.Authentifiant>>,
        origin: UsageLogCode11.From
    ): Int {
        val savedAuthentifiants = dataSaver.save(
            DataSaver.SaveRequest(
                itemsToSave = authentifiants,
                mode = DataSaver.SaveRequest.Mode.INSERT_ONLY
            )
        )
        logAccountAdded(
            savedAuthentifiants.mapNotNull { it.syncObject as? SyncObject.Authentifiant },
            origin
        )
        savedAuthentifiants.forEach {
            activityLogger.sendActivityLog(vaultItem = it, action = Action.ADD)
        }
        if (savedAuthentifiants.isNotEmpty()) {
            dataSync.sync(Trigger.SAVE)
        }
        return savedAuthentifiants.size
    }

    private fun logAccountAdded(
        authentifiants: List<SyncObject.Authentifiant>,
        origin: UsageLogCode11.From
    ) {
        if (authentifiants.isEmpty()) return 

        val teamspaceManager = teamspaceRepository.getTeamspaceManager(sessionManager.session!!)
        val usageLogRepository = bySessionUsageLogRepository[sessionManager.session] ?: return

        applicationCoroutineScope.launch {
            

            val credentialCountAfter = getCredentialsCount()

            
            val usageLogCode11 = generateUsageLog11(origin)

            val deviceCountry = deviceInfoRepository.deviceCountry

            authentifiants.forEachIndexed { index, authentifiant ->
                val teamId = TeamSpaceUtils.getTeamSpaceId(authentifiant)
                    .let { teamspaceManager?.get(it) }?.anonTeamId
                val counterAfterThisAdd = credentialCountAfter - index

                usageLogCode11.log(
                    usageLogRepository,
                    authentifiant,
                    teamId,
                    counterAfterThisAdd,
                    deviceCountry
                )
            }
        }
    }

    private fun getCredentialsCount() = dataCounter
        .count(CounterFilter(SpecificDataTypeFilter(SyncObjectType.AUTHENTIFIANT), NoSpaceFilter))

    private fun generateUsageLog11(origin: UsageLogCode11.From): UsageLogCode11 {
        return UsageLogCode11(
            type = UsageLogCode11.Type.AUTHENTICATION,
            from = origin,
            action = UsageLogCode11.Action.ADD
        )
    }

    private fun UsageLogCode11.log(
        usageLogRepository: UsageLogRepository,
        authentifiant: SyncObject.Authentifiant,
        teamId: String?,
        counterAfterThisAdd: Int,
        deviceCountry: String
    ) {
        usageLogRepository.enqueue(
            copy(
                spaceId = teamId,
                country = deviceCountry,
                counter = counterAfterThisAdd,
                itemId = authentifiant.anonId,
                details = "",
                website = authentifiant.urlForUsageLog
            )
        )
    }
}
