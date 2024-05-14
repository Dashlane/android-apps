package com.dashlane.util.clipboard.vault

import android.content.Context
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.clipboard.vault.CopyFieldContentMapper.ContentOnlyInSyncObjectException
import com.dashlane.util.toExpirationDateFormat
import com.dashlane.util.toIdentityFormat
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VaultItemFieldContentServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultDataQuery: VaultDataQuery
) : VaultItemFieldContentService {
    override fun hasContent(item: SummaryObject, copyField: CopyField): Boolean {
        val genericMapper = copyField.contentMapper
        return try {
            genericMapper.hasContent(item)
        } catch (e: ContentOnlyInSyncObjectException) {
            getVaultItem(item)?.let {
                genericMapper.hasContent(it.syncObject)
            } ?: return false
        }
    }

    override fun hasContent(vaultItem: VaultItem<*>, copyField: CopyField) =
        copyField.contentMapper.hasContent(vaultItem.syncObject)

    override fun getContent(item: SummaryObject, copyField: CopyField): String? {
        val genericMapper = copyField.contentMapper
        val genericContent = try {
            genericMapper.getContent(item)
        } catch (e: ContentOnlyInSyncObjectException) {
            getVaultItem(item)?.let {
                genericMapper.getContent(it.syncObject)
            }
        } ?: return null

        return parseContent(genericContent)
    }

    override fun getContent(vaultItem: VaultItem<*>, copyField: CopyField): String? {
        val genericContent = copyField.contentMapper.getContent(vaultItem.syncObject) ?: return null

        return parseContent(genericContent)
    }

    private fun parseContent(
        genericContent: CopyContent,
    ): String? {
        return when (genericContent) {
            is CopyContent.Ready -> genericContent.mapGenericToString()
            is CopyContent.FromRemoteItem -> {
                val remoteItem = getVaultItem(
                    itemId = genericContent.uid,
                    syncObjectType = genericContent.syncObjectType
                ) ?: return null
                getContent(remoteItem, genericContent.copyField)
            }
        }
    }

    private fun getVaultItem(item: SummaryObject): VaultItem<*>? {
        return getVaultItem(item.id, item.syncObjectType)
    }

    private fun getVaultItem(itemId: String, syncObjectType: SyncObjectType): VaultItem<*>? {
        return vaultDataQuery.query(
            vaultFilter {
                specificUid(itemId)
                specificDataType(syncObjectType)
            }
        )
    }

    private fun CopyContent.Ready.mapGenericToString(): String? {
        return when (this) {
            is CopyContent.Ready.StringValue -> content
            is CopyContent.Ready.ObfuscatedValue -> content?.toString()
            is CopyContent.Ready.Date -> content?.toIdentityFormat(context)
            is CopyContent.Ready.YearMonth -> content?.toExpirationDateFormat()
        }
    }
}
