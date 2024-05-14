package com.dashlane.securefile

import com.dashlane.network.tools.authorization
import com.dashlane.securefile.extensions.getSecureFileInfo
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.server.api.endpoints.securefile.DeleteSecureFileService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.vault.model.SyncState
import java.io.IOException
import javax.inject.Inject

class DeleteFileManager @Inject constructor(
    private val deleteSecureFileService: DeleteSecureFileService,
    private val sessionManager: SessionManager,
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val secureFileStorage: SecureFileStorage
) {
    suspend fun deleteSecureFile(secureFileInfoId: String, secureFile: SecureFile): Boolean {
        val session = sessionManager.session ?: return false
        try {
            deleteSecureFileService.execute(
                userAuthorization = session.authorization,
                request = DeleteSecureFileService.Request(secureFileInfoId)
            )
            
            vaultDataQuery.getSecureFileInfo(secureFileInfoId)?.also {
                val deletedVaultItem = it.copy(syncState = SyncState.DELETED)
                dataSaver.save(deletedVaultItem)
            }
            secureFileStorage.deleteCipheredFile(secureFile)
            return true
        } catch (_: IOException) {
            return false
        } catch (_: DashlaneApiException) {
            return false
        }
    }
}