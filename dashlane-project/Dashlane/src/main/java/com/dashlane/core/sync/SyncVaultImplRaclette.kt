package com.dashlane.core.sync

import com.dashlane.database.Id
import com.dashlane.database.VaultObjectRepository
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.vault.SyncObjectDescriptor
import com.dashlane.sync.vault.SyncVault
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.isSupportedSyncObjectType
import com.dashlane.xml.domain.toTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import kotlin.reflect.KClass

class SyncVaultImplRaclette @Inject constructor(
    private val userSettingsSyncRepository: UserSettingsSyncRepository,
    private val dataIdentifierSyncRepository: DataIdentifierSyncRepositoryRaclette,
    private val sharedPreferences: UserPreferencesManager,
    private val racletteLogger: RacletteLogger
) : SyncVault {

    override var lastSyncTime: Instant?
        get() =
            Instant.ofEpochSecond(preferences.getInt(ConstantsPrefs.TIMESTAMP_LABEL, 0).toLong())
                .takeUnless { it == Instant.EPOCH }
        set(value) {
            if (value == null) {
                preferences.putInt(ConstantsPrefs.TIMESTAMP_LABEL, 0)
            } else {
                preferences.putInt(ConstantsPrefs.TIMESTAMP_LABEL, value.epochSecond.toInt())
            }
        }

    private val preferences
        get() = sharedPreferences.preferencesForCurrentUser()!!

    override suspend fun inTransaction(block: SyncVault.TransactionScope.() -> Unit) {
        withContext(Dispatchers.IO) {
            runCatching {
                val result =
                    dataIdentifierSyncRepository.inTransaction {
                        TransactionScopeImpl(this).apply(block)
                    }
                result ?: return@withContext
                    message = "statistics = ${result.statistics}\n" +
                        "transaction = ${result.timings.transaction.duration.toMillis()}\n" +
                        "writeItems = ${result.timings.writeItems.duration.toMillis()}\n" +
                        "writeBackups = ${result.timings.writeBackups.duration.toMillis()}\n" +
                        "delete = ${result.timings.delete.duration.toMillis()}\n" +
                        "summary = ${result.timings.summary.duration.toMillis()}\n" +
                        "applyTransaction = ${result.timings.applyTransaction.duration.toMillis()}\n" +
                        "operationCount = ${result.statistics.updateCount + result.statistics.deleteCount}",
                    tag = TAG
                )
            }.onFailure {
                racletteLogger.exception(it)
            }
        }
    }

    override suspend fun prepareOutgoingOperations(types: List<SyncObjectType>) =
        withContext(Dispatchers.IO) {
            
            val dataTypes = (types - SyncObjectType.SETTINGS)
            dataIdentifierSyncRepository.preparePendingOperations(dataTypes)
        }

    override suspend fun getOutgoingTransactions(types: List<SyncObjectType>) =
        withContext(Dispatchers.IO) {
            val outgoingTransactions = dataIdentifierSyncRepository.getOutgoingTransactions()
            if (SyncObjectType.SETTINGS in types) {
                outgoingTransactions + userSettingsSyncRepository.getOutgoingTransactions()
            } else {
                outgoingTransactions
            }
        }

    override suspend fun clearOutgoingOperations(types: List<SyncObjectType>) =
        withContext(Dispatchers.IO) {
            
            dataIdentifierSyncRepository.clearPendingOperations()
            userSettingsSyncRepository.clearPendingOperations()
        }

    override suspend fun getSummary(syncObjectTypes: List<SyncObjectType>): Collection<SyncSummaryItem> =
        withContext(Dispatchers.IO) {
            val userSettingsSummary = userSettingsSyncRepository.getSummary()
            val dataIdentifiersSummary =
                dataIdentifierSyncRepository.getSummary()
            dataIdentifiersSummary + userSettingsSummary
        }

    override suspend fun fetchAsOutgoingUpdate(descriptors: Collection<SyncObjectDescriptor>): List<OutgoingTransaction.Update> =
        withContext(Dispatchers.IO) {
            val setting = descriptors.find {
                it.first == SyncObject.Settings::class
            }?.let {
                userSettingsSyncRepository.fetchAsOutgoingUpdate()
            }

            val uuids = descriptors.map { it.second }
            val list = dataIdentifierSyncRepository.fetchAsOutgoingUpdate(uuids)
            if (setting != null) {
                list + setting
            } else {
                list
            }
        }

    override suspend fun applyBackupDate(
        descriptors: Collection<SyncObjectDescriptor>,
        backupTimeMillis: Instant
    ) {
        val uuids = descriptors.map { it.second }
        dataIdentifierSyncRepository.applyBackupDate(
            uuids,
            backupTimeMillis
        )
        descriptors.find { it.first == SyncObject.Settings::class }?.also {
            userSettingsSyncRepository.applyBackupDate(backupTimeMillis)
        }
    }

    override suspend fun getDeduplicationCandidates(): List<List<OutgoingTransaction.Update>> {
        val types: List<SyncObjectType> =
            enumValues<SyncObjectType>().filter { it.hasDeduplication }
        return dataIdentifierSyncRepository.fetchDuplicate(types)
    }

    override suspend fun flagForDeletion(it: OutgoingTransaction.Update) {
        dataIdentifierSyncRepository.flagForDeletion(it)
    }

    private inner class TransactionScopeImpl(val vaultRepositoryTransaction: VaultObjectRepository.Transaction) :
        SyncVault.TransactionScope {

        override fun insertOrUpdateForSync(
            identifier: String,
            value: SyncObject,
            backupTimeMillis: Long
        ) {
            when {
                value is SyncObject.Settings ->
                    userSettingsSyncRepository.insertOrUpdateForSync(
                        value,
                        backupTimeMillis
                    )

                value.isSupportedSyncObjectType ->
                    dataIdentifierSyncRepository.getItemToSave(
                        value.toVaultItem(
                            identifier,
                            backupDate = Instant.ofEpochMilli(backupTimeMillis)
                        ),
                        value.toTransaction()
                    )?.also {
                        vaultRepositoryTransaction.update(it)
                    }

                else -> Unit 
            }
        }

        override fun deleteForSync(kClass: KClass<out SyncObject>, uuid: String) {
            when (kClass) {
                SyncObject.Settings::class -> userSettingsSyncRepository.deleteForSync()
                else -> vaultRepositoryTransaction.delete(Id.of(uuid))
            }
        }
    }

    companion object {
        private const val TAG = "DATABASE"
    }
}
