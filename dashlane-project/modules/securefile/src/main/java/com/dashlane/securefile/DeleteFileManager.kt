package com.dashlane.securefile

import com.dashlane.securefile.extensions.getSecureFileInfo
import com.dashlane.securefile.services.DeleteService
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode123
import com.dashlane.vault.model.SyncState
import java.io.IOException
import javax.inject.Inject



class DeleteFileManager @Inject constructor(
    private val deleteFileService: DeleteService,
    private val logger: SecureFileLogger,
    private val sessionManager: SessionManager,
    mainDataAccessor: MainDataAccessor,
    private val secureFileStorage: SecureFileStorage
) {
    private val dataQuery = mainDataAccessor.getVaultDataQuery()
    private val dataSaver = mainDataAccessor.getDataSaver()

    

    suspend fun deleteSecureFile(secureFileInfoId: String, secureFile: SecureFile): Boolean {
        val session = sessionManager.session ?: return false
        val secureFileInfo = dataQuery.getSecureFileInfo(secureFileInfoId)
        try {
            deleteFileService.delete(session.userId, session.uki, secureFileInfoId)
            logger.logDelete(secureFileInfo?.syncObject?.anonId)
            
            secureFileInfo?.also {
                val deletedVaultItem = it.copy(syncState = SyncState.DELETED)
                dataSaver.save(deletedVaultItem)
                val deleted = deletedVaultItem.syncObject
                logger.logFileDetails(
                    UsageLogCode123.Action.DELETE, deleted.type ?: "",
                    deleted.localSize!!.toLong(),
                    deleted.remoteSize!!.toLong(),
                    it.anonymousId
                )
            }
            secureFileStorage.deleteCipheredFile(secureFile)
            return true
        } catch (e: IOException) {
            logger.logDeleteError(secureFileInfo?.syncObject?.anonId)
            return false
        }
    }
}