package com.dashlane.vault.item.delete

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Button
import com.dashlane.hermes.generated.events.user.Click
import javax.inject.Inject

class DeleteVaultItemLogger @Inject constructor(private val logRepository: LogRepository) {
    fun logItemDeletionConfirmed() {
        logRepository.queueEvent(
            Click(Button.OK)
        )
    }

    fun logItemDeletionCanceled() {
        logRepository.queueEvent(
            Click(Button.CANCEL)
        )
    }
}