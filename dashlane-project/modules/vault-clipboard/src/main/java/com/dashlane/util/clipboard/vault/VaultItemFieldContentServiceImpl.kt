package com.dashlane.util.clipboard.vault

import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.clipboard.vault.CopyFieldContentMapper.ContentOnlyInSyncObjectException
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class VaultItemFieldContentServiceImpl @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val enumerationsContent: VaultItemVisibleCopyEdgeCases
) : VaultItemFieldContentService {
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

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
        }

        return genericContent?.mapGenericToString(copyField)
    }

    override fun getContent(vaultItem: VaultItem<*>, copyField: CopyField) =
        copyField.contentMapper.getContent(vaultItem.syncObject)?.mapGenericToString(copyField)

    private fun getVaultItem(item: SummaryObject): VaultItem<*>? {
        val itemId = item.id
        val syncObjectType = item.syncObjectType

        return vaultDataQuery.query(
            vaultFilter {
                specificUid(itemId)
                specificDataType(syncObjectType)
            }
        )
    }

    private fun Any.mapGenericToString(copyField: CopyField): String? {
        return if (enumerationsContent.shouldCopyDifferentContent(copyField)) {
            when (this) {
                is String -> enumerationsContent.mapEnumeration(this, copyField)
                is LocalDate -> enumerationsContent.mapLocalDate(this, copyField)
                is YearMonth -> enumerationsContent.mapYearMonth(this, copyField)
                else -> this.toString()
            }
        } else {
            this.toString()
        }
    }
}