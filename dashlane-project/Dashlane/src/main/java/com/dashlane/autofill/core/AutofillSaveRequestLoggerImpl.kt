package com.dashlane.autofill.core

import com.dashlane.autofill.request.save.AutofillSaveRequestLogger
import com.dashlane.autofill.util.DomainWrapper
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.SaveType
import com.dashlane.hermes.generated.events.anonymous.AutofillSuggestAnonymous
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.getTeamSpaceLog
import javax.inject.Inject

class AutofillSaveRequestLoggerImpl @Inject constructor(private val logRepository: LogRepository) :
    AutofillSaveRequestLogger {

    override fun onSave(
        itemType: ItemType,
        saveType: SaveType,
        domainWrapper: DomainWrapper,
        vaultItem: VaultItem<*>
    ) {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_NOTIFICATION_DATA_CAPTURE
        )
        logRepository.queueEvent(
            AutofillSuggest(
                isLoginPrefilled = false,
                isPasswordPrefilled = false,
                isNativeApp = domainWrapper.isNativeApp
            )
        )
        logRepository.queueEvent(
            AutofillSuggestAnonymous(
                domain = domainWrapper.domain,
                isNativeApp = domainWrapper.isNativeApp
            )
        )
        val action = saveType.toLogAction()
        logRepository.queueEvent(
            UpdateVaultItem(
                itemId = ItemId(vaultItem.uid),
                itemType = vaultItem.syncObjectType.toItemType(),
                action = action,
                space = vaultItem.getTeamSpaceLog(),
            )
        )
    }

    private fun SaveType.toLogAction(): Action {
        return when (this) {
            SaveType.REPLACE -> Action.EDIT
            SaveType.SAVE, SaveType.SAVE_AS_NEW -> Action.ADD
        }
    }
}