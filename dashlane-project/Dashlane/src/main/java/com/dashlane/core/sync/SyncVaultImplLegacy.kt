package com.dashlane.core.sync

import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.vault.SyncObjectDescriptor
import com.dashlane.sync.vault.SyncVault
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.toTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import kotlin.reflect.KClass

class SyncVaultImplLegacy @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val userSettingsSyncRepository: UserSettingsSyncRepository,
    private val dataIdentifierSyncRepository: DataIdentifierSyncRepository,
    private val sharedPreferences: UserPreferencesManager
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

    override suspend fun inTransaction(block: SyncVault.TransactionScope.() -> Unit) = withContext(Dispatchers.IO) {
        val database = userDataRepository.getDatabase(sessionManager.session!!)!!
        database.beginTransaction()
        try {
            TransactionScopeImpl().apply(block)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    override suspend fun prepareOutgoingOperations(kClasses: List<KClass<out SyncObject>>) =
        withContext(Dispatchers.IO) {
            
            val dataTypes = (kClasses - SyncObject.Settings::class)
            dataIdentifierSyncRepository.preparePendingOperations(dataTypes)
        }

    override suspend fun getOutgoingTransactions(kClasses: List<KClass<out SyncObject>>) = withContext(Dispatchers.IO) {
        kClasses.flatMap { kClass -> getOutgoingTransactions(kClass) }
    }

    private suspend fun getOutgoingTransactions(kClass: KClass<out SyncObject>): List<OutgoingTransaction> {
        return if (kClass == SyncObject.Settings::class) {
            userSettingsSyncRepository.getOutgoingTransactions()
        } else {
            dataIdentifierSyncRepository.getOutgoingTransactions(kClass)
        }
    }

    override suspend fun clearOutgoingOperations(kClasses: List<KClass<out SyncObject>>) = withContext(Dispatchers.IO) {
        
        dataIdentifierSyncRepository.clearPendingOperations()
        userSettingsSyncRepository.clearPendingOperations()
    }

    override suspend fun getSummary(syncObjectTypes: List<SyncObjectType>): Collection<SyncSummaryItem> =
        withContext(Dispatchers.IO) {
            val userSettingsSummary = userSettingsSyncRepository.getSummary()
            val dataIdentifiersSummary =
                dataIdentifierSyncRepository.getSummary(syncObjectTypes - SyncObjectType.SETTINGS)
            dataIdentifiersSummary + userSettingsSummary
        }

    override suspend fun fetchAsOutgoingUpdate(descriptors: Collection<SyncObjectDescriptor>): List<OutgoingTransaction.Update> =
        withContext(Dispatchers.IO) {
            descriptors.asSequence()
                .groupBy { it.first }
                .mapValues { (_, SyncObjectDescriptors) -> SyncObjectDescriptors.map { it.second } }
                .flatMap { (kClass, uuids) -> fetchAsOutgoingUpdate(kClass, uuids) }
        }

    private suspend fun fetchAsOutgoingUpdate(
        kClass: KClass<out SyncObject>,
        uuids: List<String>
    ): List<OutgoingTransaction.Update> {
        return if (kClass == SyncObject.Settings::class) {
            userSettingsSyncRepository.fetchAsOutgoingUpdate()
        } else {
            dataIdentifierSyncRepository.fetchAsOutgoingUpdate(kClass, uuids)
        }
    }

    override suspend fun applyBackupDate(descriptors: Collection<SyncObjectDescriptor>, backupTimeMillis: Instant) {
        descriptors.forEach { (kClass, uid) ->
            when (kClass) {
                SyncObject.Settings::class -> userSettingsSyncRepository.applyBackupDate(backupTimeMillis)
                else -> dataIdentifierSyncRepository.applyBackupDate(
                    kClass,
                    uid,
                    backupTimeMillis
                )
            }
        }
    }

    override suspend fun getDeduplicationCandidates(): List<List<OutgoingTransaction.Update>> =
        enumValues<SyncObjectType>().filter { it.hasDeduplication }
            .flatMap { dataIdentifierSyncRepository.fetchDuplicate(it) }

    override suspend fun flagForDeletion(it: OutgoingTransaction.Update) {
        dataIdentifierSyncRepository.flagForDeletion(it)
    }

    private inner class TransactionScopeImpl : SyncVault.TransactionScope {

        override fun insertOrUpdateForSync(
            identifier: String,
            value: SyncObject,
            backupTimeMillis: Long
        ) {
            when (value) {
                is SyncObject.Settings -> userSettingsSyncRepository.insertOrUpdateForSync(value, backupTimeMillis)
                else -> dataIdentifierSyncRepository.insertOrUpdateForSync(
                    value.toVaultItem(identifier),
                    value.toTransaction(),
                    backupTimeMillis
                )
            }
        }

        override fun deleteForSync(kClass: KClass<out SyncObject>, uuid: String) {
            when (kClass) {
                SyncObject.Settings::class -> userSettingsSyncRepository.deleteForSync()
                else -> dataIdentifierSyncRepository.deleteForSync(kClass, uuid)
            }
        }
    }
}