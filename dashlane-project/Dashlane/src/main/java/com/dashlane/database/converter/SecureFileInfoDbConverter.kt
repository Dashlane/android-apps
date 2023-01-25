package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.SecureFileInfoSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecureFileInfo
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object SecureFileInfoDbConverter : DbConverter.Delegate<SyncObject.SecureFileInfo> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.SecureFileInfo>) = toContentValues(item)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.SecureFileInfo> {
        val dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType())
        return createSecureFileInfo(
            dataIdentifier = dataIdentifier,
            mimeType = c.getString(SecureFileInfoSql.FIELD_MIME_TYPE),
            filename = c.getString(SecureFileInfoSql.FIELD_FILENAME),
            cryptoKey = c.getString(SecureFileInfoSql.FIELD_CRYPTO_KEY),
            downloadKey = c.getString(SecureFileInfoSql.FIELD_DOWNLOAD_KEY),
            owner = c.getString(SecureFileInfoSql.FIELD_OWNER),
            localSize = c.getString(SecureFileInfoSql.FIELD_LOCAL_SIZE),
            remoteSize = c.getString(SecureFileInfoSql.FIELD_REMOTE_SIZE),
            version = c.getString(SecureFileInfoSql.FIELD_VERSION)
        )
    }

    override fun syncObjectType() = SyncObjectType.SECURE_FILE_INFO

    override fun toContentValues(vaultItem: VaultItem<SyncObject.SecureFileInfo>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(SecureFileInfoSql.FIELD_MIME_TYPE, item.type)
        cv.put(SecureFileInfoSql.FIELD_FILENAME, item.filename)
        cv.put(SecureFileInfoSql.FIELD_CRYPTO_KEY, item.cryptoKey)
        cv.put(SecureFileInfoSql.FIELD_DOWNLOAD_KEY, item.downloadKey)
        cv.put(SecureFileInfoSql.FIELD_OWNER, item.owner)
        cv.put(SecureFileInfoSql.FIELD_LOCAL_SIZE, item.localSize)
        cv.put(SecureFileInfoSql.FIELD_REMOTE_SIZE, item.remoteSize)
        cv.put(SecureFileInfoSql.FIELD_VERSION, item.version)
        return cv
    }
}