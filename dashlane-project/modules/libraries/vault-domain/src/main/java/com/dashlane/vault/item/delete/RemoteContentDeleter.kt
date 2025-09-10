package com.dashlane.vault.item.delete

import com.dashlane.cloudpasskeys.CloudPasskeyService
import com.dashlane.vault.model.CloudPasskeyData
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import javax.inject.Inject

class RemoteContentDeleter @Inject constructor(
    private val cloudPasskeyService: CloudPasskeyService,
) {
    @Throws(RemoteContentDeletionException::class)
    suspend fun deleteFor(syncObject: SyncObject, ignoreFailures: Boolean) {
        val success: Boolean = when (syncObject.objectType) {
            SyncObjectType.PASSKEY -> {
                val passkey = (syncObject as? SyncObject.Passkey) ?: return
                val cloudPasskeyData = CloudPasskeyData.fromPasskey(passkey) ?: return

                deleteCloudPasskey(cloudPasskeyData)
            }
            else -> return
        }

        if (!success && !ignoreFailures) {
            throw RemoteContentDeletionException()
        }
    }

    private suspend fun deleteCloudPasskey(cloudPasskeyData: CloudPasskeyData): Boolean =
        cloudPasskeyService.delete(cloudPasskey = cloudPasskeyData)
}

class RemoteContentDeletionException : Exception()