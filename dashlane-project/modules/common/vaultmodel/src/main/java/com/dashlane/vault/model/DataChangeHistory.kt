package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createDataChangeHistory(
    itemId: String = generateUniqueIdentifier(),
    spaceId: String? = null,
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(uid = itemId, teamSpaceId = spaceId),
    changeSets: List<SyncObject.DataChangeHistory.ChangeSet>
): VaultItem<SyncObject.DataChangeHistory> {
        return dataIdentifier.toVaultItem(
            SyncObject.DataChangeHistory {
                this.changeSets = changeSets
                this.setCommonDataIdentifierAttrs(dataIdentifier)
            }
        )
    }