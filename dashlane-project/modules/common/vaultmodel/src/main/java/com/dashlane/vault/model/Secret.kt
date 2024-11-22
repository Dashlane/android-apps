package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createSecret(
    itemId: String = generateUniqueIdentifier(),
    spaceId: String? = null,
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(uid = itemId, teamSpaceId = spaceId),
    title: String? = null,
    content: String? = null,
    isSecured: Boolean = false
): VaultItem<SyncObject.Secret> {
    return dataIdentifier.toVaultItem(
        SyncObject.Secret {
            this.id = itemId
            this.title = title
            this.content = content
            this.secured = isSecured

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}