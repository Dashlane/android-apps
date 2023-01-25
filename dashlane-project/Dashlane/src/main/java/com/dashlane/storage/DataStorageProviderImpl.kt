package com.dashlane.storage

import android.content.Context
import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.sharing.SharingDaoImpl
import com.dashlane.core.sharing.SharingDaoImplRaclette
import com.dashlane.core.sync.SyncVaultImplLegacy
import com.dashlane.core.sync.SyncVaultImplRaclette
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.isLegacyDatabaseExist
import com.dashlane.storage.userdata.DatabaseItemSaver
import com.dashlane.storage.userdata.DatabaseItemSaverImplLegacy
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.CredentialDataQueryImplLegacy
import com.dashlane.storage.userdata.accessor.CredentialDataQueryImplRaclette
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQueryImpl
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQueryRacletteImpl
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaver
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaverLegacy
import com.dashlane.storage.userdata.accessor.DataChangeHistorySaverRaclette
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataCounterImpl
import com.dashlane.storage.userdata.accessor.DataCounterRacletteImpl
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.FrequentSearchImpl
import com.dashlane.storage.userdata.accessor.FrequentSearchRacletteImpl
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQueryImplLegacy
import com.dashlane.storage.userdata.accessor.GenericDataQueryImplRaclette
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplLegacy
import com.dashlane.storage.userdata.accessor.VaultDataQueryImplRaclette
import com.dashlane.sync.vault.SyncVault
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorageProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val genericDataQueryLegacy: GenericDataQueryImplLegacy,
    private val genericDataQueryNew: GenericDataQueryImplRaclette,
    private val syncVaultLegacy: SyncVaultImplLegacy,
    private val syncVaultNew: SyncVaultImplRaclette,
    private val vaultDataQueryLegacy: VaultDataQueryImplLegacy,
    private val vaultDataQueryNew: VaultDataQueryImplRaclette,
    private val databaseItemSaver: DatabaseItemSaverImplLegacy,
    private val databaseItemSaverNew: DatabaseItemSaverRaclette,
    private val credentialDataQueryLegacy: CredentialDataQueryImplLegacy,
    private val credentialDataQueryNew: CredentialDataQueryImplRaclette,
    private val sharingDaoLegacy: SharingDaoImpl,
    private val sharingDaoRaclette: SharingDaoImplRaclette,
    private val frequentSearchLegacy: FrequentSearchImpl,
    private val frequentSearchRacletteImpl: FrequentSearchRacletteImpl,
    private val dataCounterLegacy: DataCounterImpl,
    private val dataCounterRacletteImpl: DataCounterRacletteImpl,
    private val dataChangeHistorySaverLegacy: DataChangeHistorySaverLegacy,
    private val dataChangeHistorySaverRaclette: DataChangeHistorySaverRaclette,
    private val dataChangeHistoryQueryLegacy: DataChangeHistoryQueryImpl,
    private val dataChangeHistoryQueryRaclette: DataChangeHistoryQueryRacletteImpl
) : DataStorageProvider {

    override val useRaclette: Boolean
        get() = !(sessionManager.session?.isLegacyDatabaseExist(context) ?: false)

    override val genericDataQuery: GenericDataQuery
        get() {
            return if (useRaclette) {
                genericDataQueryNew
            } else {
                genericDataQueryLegacy
            }
        }

    override val syncVault: SyncVault
        get() = if (useRaclette) {
            syncVaultNew
        } else {
            syncVaultLegacy
        }

    override val vaultDataQuery: VaultDataQuery
        get() = if (useRaclette) {
            vaultDataQueryNew
        } else {
            vaultDataQueryLegacy
        }

    override val itemSaver: DatabaseItemSaver
        get() = if (useRaclette) {
            databaseItemSaverNew
        } else {
            databaseItemSaver
        }

    override val credentialDataQuery: CredentialDataQuery
        get() = if (useRaclette) {
            credentialDataQueryNew
        } else {
            credentialDataQueryLegacy
        }

    override val sharingDao: SharingDao
        get() = if (useRaclette) {
            sharingDaoRaclette
        } else {
            sharingDaoLegacy
        }

    override val frequentSearch: FrequentSearch
        get() = if (useRaclette) {
            frequentSearchRacletteImpl
        } else {
            frequentSearchLegacy
        }

    override val dataCounter: DataCounter
        get() = if (useRaclette) {
            dataCounterRacletteImpl
        } else {
            dataCounterLegacy
        }

    override val dataChangeHistorySaver: DataChangeHistorySaver
        get() = if (useRaclette) {
            dataChangeHistorySaverRaclette
        } else {
            dataChangeHistorySaverLegacy
        }

    override val dataChangeHistoryQuery: DataChangeHistoryQuery
        get() = if (useRaclette) {
            dataChangeHistoryQueryRaclette
        } else {
            dataChangeHistoryQueryLegacy
        }
}