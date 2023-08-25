package com.dashlane.securefile.extensions

import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.securefile.SecureFile
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecureFileInfo
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

fun SecureFile.toSecureFileInfo(username: String): VaultItem<SyncObject.SecureFileInfo> {
    val timestamp = Instant.now()
    return createSecureFileInfo(
        dataIdentifier = CommonDataIdentifierAttrsImpl(
            creationDate = timestamp,
            userModificationDate = timestamp
        ),
        filename = this@toSecureFileInfo.fileName,
        downloadKey = this@toSecureFileInfo.id,
        cryptoKey = this@toSecureFileInfo.key.encodeBase64ToString(),
        remoteSize = this@toSecureFileInfo.encryptedFile?.value?.length()?.toInt()?.toString(),
        owner = username,
        version = "1"
    )
}

@Suppress("UNCHECKED_CAST")
suspend fun VaultDataQuery.getSecureFileInfo(uid: String) = withContext(Dispatchers.IO) {
    query(
        vaultFilter {
        specificUid(uid)
        specificDataType(SyncObjectType.SECURE_FILE_INFO)
    }
    ) as? VaultItem<SyncObject.SecureFileInfo>
}
