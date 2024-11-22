package com.dashlane.masterpassword

import com.dashlane.accountrecoverykey.AccountRecoveryKeyRepository
import com.dashlane.accountrecoverykey.AccountRecoveryKeySettingStateRefresher
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.hermes.generated.definitions.DeleteKeyReason
import com.dashlane.server.api.endpoints.sync.MasterPasswordUploadService
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class ChangeMasterPasswordDataProvider @Inject constructor(
    private val masterPasswordChanger: MasterPasswordChanger,
    private val accountRecoveryKeyRepository: AccountRecoveryKeyRepository,
    private val accountRecoveryKeySettingStateRefresher: AccountRecoveryKeySettingStateRefresher,
) :
    BaseDataProvider<ChangeMasterPasswordContract.Presenter>(), ChangeMasterPasswordContract.DataProvider {

    override val progressStateFlow = masterPasswordChanger.progressStateFlow

    override suspend fun clearChannel() {
        masterPasswordChanger.reset()
    }

    override suspend fun updateMasterPassword(newPassword: ObfuscatedByteArray, origin: ChangeMasterPasswordOrigin) {
        val uploadReason = if (origin is ChangeMasterPasswordOrigin.Recovery) {
            MasterPasswordUploadService.Request.UploadReason.MASTER_PASSWORD_MOBILE_RESET
        } else {
            null
        }
        val result = masterPasswordChanger.updateMasterPassword(newPassword, uploadReason)
        if (result) {
            accountRecoveryKeyRepository.disableRecoveryKey(DeleteKeyReason.VAULT_KEY_CHANGED)
            accountRecoveryKeySettingStateRefresher.refresh()
        }
    }

    override suspend fun migrateToMasterPasswordUser(password: ObfuscatedByteArray, authTicket: String) {
        masterPasswordChanger.migrateToMasterPasswordUser(password, authTicket)
    }
}