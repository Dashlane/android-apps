package com.dashlane.core.domain

import android.content.Context
import com.dashlane.core.DataSync
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.formatTitle
import com.dashlane.vault.util.copyWithDefaultValue
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountStorageImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val mainDataAccessor: MainDataAccessor,
    private val sessionManager: SessionManager,
    private val dataSync: DataSync,
    private val activityLogger: VaultActivityLogger
) : ImportAuthentifiantHelper, VpnThirdPartyAuthentifiantHelper {

    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()

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
        addAuthentifiants(listOf(authentifiant))
    }

    override suspend fun addAuthentifiants(
        authentifiants: List<VaultItem<SyncObject.Authentifiant>>
    ): Int {
        val savedAuthentifiants = dataSaver.save(
            DataSaver.SaveRequest(
                itemsToSave = authentifiants,
                mode = DataSaver.SaveRequest.Mode.INSERT_ONLY
            )
        )
        savedAuthentifiants.forEach {
            activityLogger.sendActivityLog(vaultItem = it, action = Action.ADD)
        }
        if (savedAuthentifiants.isNotEmpty()) {
            dataSync.sync(Trigger.SAVE)
        }
        return savedAuthentifiants.size
    }
}
