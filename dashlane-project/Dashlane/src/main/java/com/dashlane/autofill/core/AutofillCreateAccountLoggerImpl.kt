package com.dashlane.autofill.core

import com.dashlane.autofill.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.util.DomainWrapper
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.util.getTeamSpaceLog
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class AutofillCreateAccountLoggerImpl @Inject constructor(
    private val logRepository: LogRepository
) : AutofillCreateAccountLogger {

    override fun onCancel(domainWrapper: DomainWrapper) {
        val dismissType = DismissType.CLOSE

        logRepository.queueEvent(
            AutofillDismiss(
                dismissType = dismissType
            )
        )
        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = dismissType,
                domain = domainWrapper.domain,
                isNativeApp = domainWrapper.isNativeApp
            )
        )
    }

    override fun logSave(
        domainWrapper: DomainWrapper,
        credential: VaultItem<SyncObject.Authentifiant>
    ) {
        val dataType = listOf(ItemType.CREDENTIAL)
        logRepository.queueEvent(
            AutofillAccept(dataTypeList = dataType)
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(domain = domainWrapper.domain)
        )
        logRepository.queueEvent(
            UpdateVaultItem(
                itemId = ItemId(credential.uid),
                itemType = ItemType.CREDENTIAL,
                action = Action.ADD,
                space = credential.getTeamSpaceLog(),
            )
        )
    }
}