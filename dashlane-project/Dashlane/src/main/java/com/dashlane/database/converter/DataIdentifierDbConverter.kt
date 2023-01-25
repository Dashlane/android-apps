package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.sql.TeamSpaceSupportingItemSql
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.util.getInt
import com.dashlane.util.getLong
import com.dashlane.util.getString
import com.dashlane.util.isValueNull
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.CommonDataIdentifierAttrs
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.signature
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.vault.util.isSpaceSupported
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country
import java.lang.IllegalArgumentException
import java.time.Instant

object DataIdentifierDbConverter {

    internal fun loadDataIdentifier(c: Cursor, dataType: SyncObjectType): CommonDataIdentifierAttrs {
        return CommonDataIdentifierAttrsImpl(
            uid = c.getString(DataIdentifierSql.FIELD_UID) ?: "",
            anonymousUID = c.getString(DataIdentifierSql.FIELD_ANONYMOUS_UID) ?: "",
            id = c.getInt(DataIdentifierSql.FIELD_ID),
            formatLang = try { Country.forSignature(c.getInt(DataIdentifierSql.FIELD_LOCALE_LANG)) } catch (e: IllegalArgumentException) { null },
            creationDate = c.getString(DataIdentifierSql.FIELD_CREATION_DATE)?.toInstant(),
            userModificationDate = c.getString(DataIdentifierSql.FIELD_USER_MODIFICATION_DATE)?.toInstant(),
            locallyViewedDate = c.getString(DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE)?.toInstant(),
            locallyUsedCount = c.getLong(DataIdentifierSql.FIELD_LOCALLY_USED_COUNT),
            syncState = c.getString(DataIdentifierSql.FIELD_ITEM_STATE)
                ?.let { tryOrNull { SyncState.fromCode(it) } } ?: SyncState.SYNCED,
            teamSpaceId = if (dataType.isSpaceSupported) {
                c.getString(TeamSpaceSupportingItemSql.LABEL_TEAMSPACE)
                    ?.takeUnless { it.isValueNull() }
                    ?: TeamspaceManager.PERSONAL_TEAMSPACE.teamId
            } else {
                null
            },
            attachments = c.getString(DataIdentifierSql.FIELD_ATTACHMENTS)
        )
    }

    internal fun loadDataIdentifier(c: ContentValues, dataType: SyncObjectType): CommonDataIdentifierAttrs {
        return CommonDataIdentifierAttrsImpl(
            uid = c.getAsString(DataIdentifierSql.FIELD_UID) ?: "",
            anonymousUID = c.getAsString(DataIdentifierSql.FIELD_ANONYMOUS_UID) ?: "",
            id = c.getAsInteger(DataIdentifierSql.FIELD_ID),
            formatLang = Country.forSignature(c.getAsInteger(DataIdentifierSql.FIELD_LOCALE_LANG)),
            creationDate = c.getAsString(DataIdentifierSql.FIELD_CREATION_DATE)?.toInstant(),
            userModificationDate = c.getAsString(DataIdentifierSql.FIELD_USER_MODIFICATION_DATE)?.toInstant(),
            locallyViewedDate = c.getAsString(DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE)?.toInstant(),
            locallyUsedCount = c.getAsLong(DataIdentifierSql.FIELD_LOCALLY_USED_COUNT),
            syncState = c.getAsString(DataIdentifierSql.FIELD_ITEM_STATE)
                ?.let { tryOrNull { SyncState.fromCode(it) } } ?: SyncState.SYNCED,
            teamSpaceId = if (dataType.isSpaceSupported) {
                c.getAsString(TeamSpaceSupportingItemSql.LABEL_TEAMSPACE)
                    ?.takeUnless { it.isValueNull() }
                    ?: TeamspaceManager.PERSONAL_TEAMSPACE.teamId
            } else {
                null
            },
            attachments = c.getAsString(DataIdentifierSql.FIELD_ATTACHMENTS)
        )
    }

    private fun String?.toInstant(): Instant? {
        val time = this?.toLongOrNull() ?: return null
        if (time == 0L) {
            return null
        }
        return Instant.ofEpochMilli(time)
    }

    internal fun getContentValues(vaultItem: VaultItem<*>): ContentValues {
        val cv = toContentValues(vaultItem)
        if (vaultItem.isSpaceItem()) {
            cv.put(TeamSpaceSupportingItemSql.LABEL_TEAMSPACE, TeamSpaceUtils.getTeamSpaceId(vaultItem))
        }
        return cv
    }

    internal fun toContentValues(attrs: CommonDataIdentifierAttrs): ContentValues {
        val cv = ContentValues()
        cv.put(DataIdentifierSql.FIELD_UID, attrs.uid)
        cv.put(DataIdentifierSql.FIELD_ANONYMOUS_UID, attrs.anonymousUID)
        cv.put(DataIdentifierSql.FIELD_LOCALE_LANG, attrs.formatLang?.signature)
        cv.put(DataIdentifierSql.FIELD_ITEM_STATE, attrs.syncState.code)
        val kwAttachments = attrs.attachments
        if (kwAttachments != null) {
            cv.put(DataIdentifierSql.FIELD_ATTACHMENTS, attrs.attachments)
        }
        cv.put(
            DataIdentifierSql.FIELD_CREATION_DATE,
            attrs.creationDate?.toMillisString()
        )
        cv.put(
            DataIdentifierSql.FIELD_USER_MODIFICATION_DATE,
            attrs.userModificationDate?.toMillisString() ?: "0"
        )
        cv.put(
            DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE,
            attrs.locallyViewedDate?.toMillisString() ?: "0"
        )
        cv.put(
            DataIdentifierSql.FIELD_LOCALLY_USED_COUNT,
            attrs.locallyUsedCount
        )
        return cv
    }

    private fun toContentValues(attrs: VaultItem<*>): ContentValues {
        val cv = ContentValues()
        cv.put(DataIdentifierSql.FIELD_UID, attrs.uid)
        cv.put(DataIdentifierSql.FIELD_ANONYMOUS_UID, attrs.anonymousId)
        cv.put(DataIdentifierSql.FIELD_LOCALE_LANG, attrs.syncObject.localeFormat.signature)
        try {
            cv.put(DataIdentifierSql.FIELD_ITEM_STATE, attrs.syncState.code)
        } catch (e: Exception) {
            cv.put(DataIdentifierSql.FIELD_ITEM_STATE, SyncState.MODIFIED.code)
        }
        val kwAttachments = attrs.syncObject.attachments
        if (kwAttachments != null) {
            cv.put(DataIdentifierSql.FIELD_ATTACHMENTS, kwAttachments)
        }

        cv.put(
            DataIdentifierSql.FIELD_CREATION_DATE,
            attrs.syncObject.creationDatetime.toMillisString()
        )
        cv.put(
            DataIdentifierSql.FIELD_USER_MODIFICATION_DATE,
            attrs.syncObject.userModificationDatetime?.toMillisString() ?: "0"
        )
        cv.put(
            DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE,
            attrs.locallyViewedDate?.toMillisString() ?: "0"
        )
        cv.put(
            DataIdentifierSql.FIELD_LOCALLY_USED_COUNT,
            attrs.locallyUsedCount
        )
        val userContentData = attrs.syncObject.userContentData
        if (userContentData != null) {
            cv.put(
                DataIdentifierSql.FIELD_USER_DATA_HASH,
                userContentData.hashCode()
            )
        }
        return cv
    }

    private fun Instant?.toMillisString() = this?.toEpochMilli()?.toString()
}
