package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObject.WiFiEncryptionType

@Suppress("kotlin:S107")
fun createWifi(
    itemId: String = generateUniqueIdentifier(),
    spaceId: String? = null,
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(uid = itemId, teamSpaceId = spaceId),
    ssid: String? = null,
    passphrase: SyncObfuscatedValue? = null,
    note: String? = null,
    hidden: Boolean = false,
    encryptionType: WiFiEncryptionType? = null,
): VaultItem<SyncObject.WiFi> {
    return dataIdentifier.toVaultItem(
        SyncObject.WiFi {
            this.id = itemId
            this.ssid = ssid
            this.passphrase = passphrase
            this.hidden = hidden
            this.encryptionType = encryptionType
            this.note = note

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        },
    )
}