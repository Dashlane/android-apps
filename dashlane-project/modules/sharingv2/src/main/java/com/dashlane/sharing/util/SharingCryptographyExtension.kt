package com.dashlane.sharing.util

import com.dashlane.cryptography.CryptographyKey
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemUpload
import com.dashlane.sharing.internal.model.ItemToShare

fun SharingCryptographyHelper.generateItemsUpload(
    item: ItemToShare,
    groupKey: CryptographyKey.Raw32
): List<ItemUpload>? {
    val itemKey: CryptographyKey.Raw32 = newGroupKey()
    val itemKeyEncrypted: String =
        encryptItemKey(itemKey.toByteArray(), groupKey)
    val encryptedContent: String = encryptItemContent(item.content, itemKey) ?: return null
    val itemUpload =
        ItemUpload(itemId = ItemUpload.ItemId(item.itemId), itemKey = itemKeyEncrypted, content = encryptedContent)
    return listOf(itemUpload)
}