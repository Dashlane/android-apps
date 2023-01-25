package com.dashlane.item.delete

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.events.user.Click
import javax.inject.Inject

class DeleteVaultItemLogger @Inject constructor(private val logRepository: LogRepository) :
    DeleteVaultItemContract.Logger {
    override fun logItemDeletionConfirmed() {
        logRepository.queueEvent(
            Click(Button.OK)
        )
    }

    override fun logItemDeletionCanceled() {
        logRepository.queueEvent(
            Click(Button.CANCEL)
        )
    }
}