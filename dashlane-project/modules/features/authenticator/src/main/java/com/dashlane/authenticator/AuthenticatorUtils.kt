package com.dashlane.authenticator

import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant

@Suppress("UNCHECKED_CAST")
internal suspend fun updateOtp(
    itemId: String,
    otp: Otp?,
    vaultDataQuery: VaultDataQuery,
    dataSaver: DataSaver,
    logger: AuthenticatorLogger,
    updateModificationDate: Boolean = false
) {
    (vaultDataQuery.query(
        vaultFilter {
            ignoreUserLock()
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(itemId)
        }
    ) as? VaultItem<SyncObject.Authentifiant>)?.let { item ->
        val updatedItem = item.copy(syncObject = item.syncObject.copy {
            otpUrl = otp?.url?.toSyncObfuscatedValue()
            otpSecret = if (otp?.isStandardOtp() == true) {
                otp.secret?.toSyncObfuscatedValue()
            } else {
                null
            }
        }).copyWithAttrs {
            syncState = SyncState.MODIFIED
            if (updateModificationDate) userModificationDate = Instant.now()
        }
        dataSaver.save(updatedItem)
        logger.logUpdateCredential(updatedItem.uid)
    }
}