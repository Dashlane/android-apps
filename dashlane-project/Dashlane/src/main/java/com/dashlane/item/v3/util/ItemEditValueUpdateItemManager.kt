package com.dashlane.item.v3.util

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

fun interface ItemEditValueUpdateItemManager<Form : FormData, T : SyncObject> {
    fun updateWithData(
        data: Data<Form>,
        initialVaultItem: VaultItem<T>,
    ): Pair<VaultItem<T>, Set<Field>>
}