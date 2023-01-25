package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.SecurityBreachSql
import com.dashlane.util.asOptStringSequence
import com.dashlane.util.getInt
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecurityBreach
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import org.json.JSONArray

object SecurityBreachDbConverter : DbConverter.Delegate<SyncObject.SecurityBreach> {

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.SecurityBreach> {
        return createSecurityBreach(
            DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            breachId = c.getString(SecurityBreachSql.FIELD_BREACH_ID)!!,
            status = c.getString(SecurityBreachSql.FIELD_STATUS)?.let { SyncObjectEnum.getEnumForValue(it) },
            content = c.getString(SecurityBreachSql.FIELD_CONTENT),
            contentRevision = c.getInt(SecurityBreachSql.FIELD_CONTENT_REVISION),
            leakedPasswords = c.getString(SecurityBreachSql.FIELD_LEAK_PASSWORDS)
                ?.let { unserialize(it) }
                ?: setOf()
        )
    }

    override fun syncObjectType() = SyncObjectType.SECURITY_BREACH

    override fun toContentValues(vaultItem: VaultItem<SyncObject.SecurityBreach>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(SecurityBreachSql.FIELD_BREACH_ID, item.breachId)
        cv.put(SecurityBreachSql.FIELD_CONTENT, item.content ?: "")
        cv.put(SecurityBreachSql.FIELD_CONTENT_REVISION, item.contentRevision)
        cv.put(SecurityBreachSql.FIELD_STATUS, item.status?.value)
        cv.put(SecurityBreachSql.FIELD_LEAK_PASSWORDS, item.leakedPasswords?.toString())
        return cv
    }

    private fun unserialize(serialized: String): Set<String> {
        return try {
            val jsonArray = JSONArray(serialized)
            jsonArray.asOptStringSequence().filterNotNull().toSet()
        } catch (e: Exception) {
            setOf()
        }
    }
}