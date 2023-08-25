package com.dashlane.autofill.api.request.save

import com.dashlane.autofill.api.util.DomainWrapper
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.SaveType
import com.dashlane.vault.model.VaultItem

interface AutofillSaveRequestLogger {

    fun onSave(
        itemType: ItemType,
        saveType: SaveType,
        domainWrapper: DomainWrapper,
        vaultItem: VaultItem<*>
    )
}