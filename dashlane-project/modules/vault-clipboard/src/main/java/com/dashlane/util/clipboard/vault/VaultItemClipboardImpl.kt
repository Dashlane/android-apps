package com.dashlane.util.clipboard.vault

import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.storage.userdata.accessor.markedAsSearchedAsync
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject



class VaultItemClipboardImpl @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val dataSaver: DataSaver,
    private val clipboardCopy: ClipboardCopy,
    private val vaultItemCopyListenerHolder: VaultItemCopyListenerHolder,
    private val vaultItemFieldContentService: VaultItemFieldContentService
) : VaultItemClipboard {

    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    private val frequentSearch: FrequentSearch
        get() = mainDataAccessor.getFrequentSearch()

    override fun handleCopy(
        notificationId: String,
        content: String,
        copyField: CopyField
    ): Boolean {
        clipboardCopy.copyToClipboard(content, copyField.isSensitiveData, true, copyField.getFeedback())

        vaultItemCopyListenerHolder.getVaultItemCopyListener().forEach {
            it.onCopyFromFollowUpNotification(notificationId, copyField)
        }
        return true
    }

    override fun handleCopy(itemId: String, copyField: CopyField, syncObjectType: SyncObjectType): Boolean =
        getSyncObjectFromVault(itemId, syncObjectType)?.let {
            handleCopy(it, copyField)
        } ?: false

    

    override fun handleCopy(vaultItem: VaultItem<*>, copyField: CopyField): Boolean {
        if (!vaultItemFieldContentService.hasContent(vaultItem, copyField)) {
            return false
        }

        val content = vaultItemFieldContentService.getContent(vaultItem, copyField) ?: return false

        clipboardCopy.copyToClipboard(content, copyField.isSensitiveData, true, copyField.getFeedback())
        vaultItemCopyListenerHolder.getVaultItemCopyListener().forEach { copyListener ->
            copyListener.onCopyFromVault(
                vaultItem.toSummary(),
                copyField
            )
        }

        return true
    }

    

    override fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        updateLocalUsage: Boolean,
        updateFrequentSearch: Boolean,
        highlight: Highlight?,
        index: Double?,
        totalCount: Int?
    ): Boolean {
        if (!vaultItemFieldContentService.hasContent(item, copyField)) {
            return false
        }
        val content = vaultItemFieldContentService.getContent(item, copyField) ?: return false

        if (updateFrequentSearch) {
            frequentSearch.markedAsSearchedAsync(item.id, item.syncObjectType)
        }

        if (updateLocalUsage) {
            markAsViewed(item)
        }

        clipboardCopy.copyToClipboard(content, copyField.isSensitiveData, true, copyField.getFeedback())
        vaultItemCopyListenerHolder.getVaultItemCopyListener().forEach {
            it.onCopyFromVault(item, copyField, highlight, index, totalCount)
        }

        return true
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun markAsViewed(item: SummaryObject) {
        
        GlobalScope.launch {
            val vaultItem = vaultDataQuery.query(vaultFilter {
                specificUid(item.id)
            }) ?: return@launch
            dataSaver.save(vaultItem.copyWithAttrs {
                locallyViewedDate = Instant.now()
                locallyUsedCount = vaultItem.locallyUsedCount + 1
            })
        }
    }

    private fun getSyncObjectFromVault(itemId: String, syncObjectType: SyncObjectType) = vaultDataQuery.query(
        vaultFilter {
            specificUid(itemId)
            specificDataType(syncObjectType)
        })

    

    override fun hasContent(item: SummaryObject, copyField: CopyField): Boolean {
        if (UserPermission.LIMITED == item.sharingPermission) return false

        return vaultItemFieldContentService.hasContent(item, copyField)
    }
}