package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.SocialSecurityStatementSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSocialSecurityStatement
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object SocialSecurityStatementDbConverter : DbConverter.Delegate<SyncObject.SocialSecurityStatement> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.SocialSecurityStatement> {
        val sex = c.getString(SocialSecurityStatementSql.FIELD_SEX)?.let {
            when (it.toIntOrNull()) {
                0 -> SyncObject.Gender.MALE
                1 -> SyncObject.Gender.FEMALE
                else -> null
            }
        }
        return createSocialSecurityStatement(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            sex = sex,
            socialSecurityFullname = c.getString(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_FULLNAME),
            socialSecurityNumber = c.getString(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_NUMBER),
            linkedIdentity = c.getString(SocialSecurityStatementSql.FIELD_LINKED_IDENTITY),
            dateOfBirth = TimedDocumentDbConverter.getLocalDate(c, SocialSecurityStatementSql.FIELD_DATE_OF_BIRTH)
        )
    }

    override fun syncObjectType() = SyncObjectType.SOCIAL_SECURITY_STATEMENT

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.SocialSecurityStatement>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.SocialSecurityStatement>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)
        val genderValue = when (item.sex) {
            SyncObject.Gender.MALE -> "0"
            SyncObject.Gender.FEMALE -> "1"
            else -> null
        }

        cv.put(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_NUMBER, item.socialSecurityNumber?.toString())
        cv.put(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_FULLNAME, item.socialSecurityFullname)
        cv.put(SocialSecurityStatementSql.FIELD_LINKED_IDENTITY, item.linkedIdentity)
        TimedDocumentDbConverter.putLocalDate(cv, SocialSecurityStatementSql.FIELD_DATE_OF_BIRTH, item.dateOfBirth)
        try {
            cv.put(SocialSecurityStatementSql.FIELD_SEX, genderValue)
        } catch (e: Exception) {
            
        }

        return cv
    }
}
