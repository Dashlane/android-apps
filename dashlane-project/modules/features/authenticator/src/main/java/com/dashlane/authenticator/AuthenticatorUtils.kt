package com.dashlane.authenticator

import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant

@Suppress("UNCHECKED_CAST")
internal suspend fun updateOtp(
    itemId: String,
    otp: Otp?,
    vaultDataQuery: VaultDataQuery,
    dataSaver: DataSaver,
    dataSync: DataSync,
    logger: AuthenticatorLogger,
    updateModificationDate: Boolean = false
) {
    (
        vaultDataQuery.query(
            vaultFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.AUTHENTIFIANT)
                specificUid(itemId)
            }
        ) as? VaultItem<SyncObject.Authentifiant>
        )?.let { item ->
            val updatedItem = item.copy(
                syncObject = item.syncObject.copy {
                    otpUrl = otp?.url?.toSyncObfuscatedValue() ?: SyncObfuscatedValue("")
                    otpSecret = if (otp?.isStandardOtp() == true) {
                        otp.secret?.toSyncObfuscatedValue()
                    } else {
                        null
                    } ?: SyncObfuscatedValue("")
                }
            ).copyWithAttrs {
                syncState = SyncState.MODIFIED
                if (updateModificationDate) userModificationDate = Instant.now()
            }
            dataSaver.save(updatedItem)
            dataSync.sync(Trigger.SAVE)
            logger.logUpdateCredential(updatedItem.uid)
        }
}