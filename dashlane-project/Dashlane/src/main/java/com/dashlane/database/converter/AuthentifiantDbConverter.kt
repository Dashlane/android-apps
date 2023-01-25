package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.util.getBoolean
import com.dashlane.util.getString
import com.dashlane.vault.model.DataIdentifierAttrsMutable
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.toJson
import com.dashlane.xml.XmlData
import com.dashlane.xml.asJsonDataClassOrNull
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant

object AuthentifiantDbConverter : DbConverter.Delegate<SyncObject.Authentifiant> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Authentifiant> {
        val dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType())
        return createAuthentifiant(
            dataIdentifier = DataIdentifierAttrsMutable.with(dataIdentifier) {
                sharingPermission = c.getString(DataIdentifierSql.FIELD_SHARING_PERMISSION)
                hasDirtySharedField = c.getBoolean(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD)
            },
            title = c.getString(AuthentifiantSql.FIELD_TITLE),
            deprecatedUrl = c.getString(AuthentifiantSql.FIELD_URL_DEPRECATED),
            userSelectedUrl = c.getString(AuthentifiantSql.FIELD_USER_SELECTED_URL),
            useFixedUrl = c.getString(AuthentifiantSql.FIELD_USE_FIXED_URL)?.toBoolean() == true,
            email = c.getString(AuthentifiantSql.FIELD_AUTH_EMAIL),
            login = c.getString(AuthentifiantSql.FIELD_AUTH_LOGIN),
            password = c.getString(AuthentifiantSql.FIELD_AUTH_PASSWORD)?.let { SyncObfuscatedValue(it) },
            otpSecret = c.getString(AuthentifiantSql.FIELD_AUTH_OTP_SECRET)?.let { SyncObfuscatedValue(it) },
            otpUrl = c.getString(AuthentifiantSql.FIELD_AUTH_OTP_URL)?.let { SyncObfuscatedValue(it) },
            authExtra = c.getString(AuthentifiantSql.FIELD_AUTH_EXTRA),
            category = c.getString(AuthentifiantSql.FIELD_AUTH_CATEGORY),
            note = c.getString(AuthentifiantSql.FIELD_AUTH_NOTE),
            autoLogin = c.getString(AuthentifiantSql.FIELD_AUTH_AUTOLOGIN),
            numberUses = c.getString(AuthentifiantSql.FIELD_AUTH_NUMBERUSE),
            lastUse = c.getString(AuthentifiantSql.FIELD_AUTH_LASTUSE),
            strength = c.getString(AuthentifiantSql.FIELD_AUTH_STRENGTH),
            authMeta = c.getString(AuthentifiantSql.FIELD_AUTH_META)?.let {
                XmlData.ItemNode(it).asJsonDataClassOrNull()
            },
            passwordModificationDate = c.getString(AuthentifiantSql.FIELD_AUTH_PASSWORD_MODIFICATION_DATE)
                ?.toLongOrNull()
                ?.let { Instant.ofEpochMilli(it) },
            isChecked = c.getString(AuthentifiantSql.FIELD_CHECKED)?.toBoolean() == true,
            linkedServices = c.getString(AuthentifiantSql.FIELD_AUTH_LINKED_SERVICES)?.let {
                XmlData.ItemNode(it).asJsonDataClassOrNull()
            },
        )
    }

    override fun syncObjectType() = SyncObjectType.AUTHENTIFIANT

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Authentifiant>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Authentifiant>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(AuthentifiantSql.FIELD_TITLE, item.title)
        cv.put(AuthentifiantSql.FIELD_URL_DEPRECATED, item.url)
        cv.put(AuthentifiantSql.FIELD_AUTH_EMAIL, item.email)
        cv.put(AuthentifiantSql.FIELD_AUTH_LOGIN, item.login)
        cv.put(AuthentifiantSql.FIELD_AUTH_PASSWORD, item.password?.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_OTP_SECRET, item.otpSecret?.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_OTP_URL, item.otpUrl?.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_EXTRA, item.secondaryLogin)
        cv.put(AuthentifiantSql.FIELD_AUTH_CATEGORY, item.category)
        cv.put(AuthentifiantSql.FIELD_AUTH_NOTE, item.note)
        cv.put(AuthentifiantSql.FIELD_AUTH_AUTOLOGIN, item.autoLogin?.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_NUMBERUSE, item.numberUse?.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_LASTUSE, item.lastUse?.epochSecond?.toString() ?: "0")
        cv.put(AuthentifiantSql.FIELD_AUTH_STRENGTH, item.strength?.toString())
        cv.put(AuthentifiantSql.FIELD_USER_SELECTED_URL, item.userSelectedUrl)
        cv.put(AuthentifiantSql.FIELD_USE_FIXED_URL, item.useFixedUrl.toString())
        cv.put(AuthentifiantSql.FIELD_AUTH_META, item.appMetaData?.toJson())
        cv.put(AuthentifiantSql.FIELD_CHECKED, item.checked.toString())
        cv.put(
            AuthentifiantSql.FIELD_AUTH_PASSWORD_MODIFICATION_DATE,
            item.modificationDatetime?.toEpochMilli()?.toString() ?: "0"
        )
        cv.put(AuthentifiantSql.FIELD_AUTH_LINKED_SERVICES, item.linkedServices?.toJson())
        cv.put(DataIdentifierSql.FIELD_ITEM_STATE, vaultItem.syncState.code)
        cv.put(DataIdentifierSql.FIELD_SHARING_PERMISSION, vaultItem.sharingPermission)
        cv.put(DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD, vaultItem.hasDirtySharedField)
        return cv
    }
}
