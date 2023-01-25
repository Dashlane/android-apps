package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject



fun createSecureFileInfo(
    dataIdentifier: CommonDataIdentifierAttrs,
    mimeType: String? = null,
    filename: String? = null,
    downloadKey: String? = null,
    cryptoKey: String? = null,
    localSize: String? = null,
    remoteSize: String? = null,
    owner: String? = null,
    version: String? = null
): VaultItem<SyncObject.SecureFileInfo> {
    return dataIdentifier.toVaultItem(
        SyncObject.SecureFileInfo {
            this.type = mimeType
            this.filename = filename
            this.downloadKey = downloadKey
            this.cryptoKey = cryptoKey
            this.localSize = localSize?.toLongOrNull()
            this.remoteSize = remoteSize?.toLongOrNull()
            this.owner = owner
            this.version = version

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.SecureFileInfo>.copySyncObject(builder: SyncObject.SecureFileInfo.Builder.() -> Unit = {}):
        VaultItem<SyncObject.SecureFileInfo> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
