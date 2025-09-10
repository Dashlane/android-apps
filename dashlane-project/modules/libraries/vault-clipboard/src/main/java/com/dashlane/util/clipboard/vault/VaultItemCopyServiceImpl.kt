package com.dashlane.util.clipboard.vault

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.sharing.UserPermission
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.util.toHighlight
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class VaultItemCopyServiceImpl @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val frequentSearch: FrequentSearch,
    private val dataSaver: DataSaver,
    private val clipboardCopy: ClipboardCopy,
    private val vaultItemCopyListenerHolder: VaultItemCopyListenerHolder,
    private val vaultItemFieldContentService: VaultItemFieldContentService,
    private val frozenStateManager: FrozenStateManager,
    private val vaultItemCopyLogger: VaultItemCopyLogger,
    private val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>,
) : VaultItemCopyService {
    private val sessionCoroutineScope: CoroutineScope?
        get() = sessionCoroutineScopeProvider.get()

    override fun handleCopy(
        notificationId: String,
        content: String,
        copyField: CopyField,
    ): Boolean {
        clipboardCopy.copyToClipboard(
            data = content,
            sensitiveData = copyField.isSensitiveData,
            autoClear = true,
            feedback = copyField.getFeedback(),
        )

        vaultItemCopyListenerHolder.getVaultItemCopyListener()
            .forEach { it.onCopyFromFollowUpNotification(notificationId, copyField) }
        return true
    }

    override fun handleCopy(
        itemId: String,
        copyField: CopyField,
    ): Boolean = getSyncObjectFromVault(itemId)?.let { handleCopy(it, copyField) } ?: false

    override fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        itemListContext: ItemListContext,
    ): Boolean = handleCopy(
        item = item,
        copyField = copyField,
        updateFrequentSearch = itemListContext.container == ItemListContext.Container.SEARCH,
        highlight = itemListContext.section.toHighlight(),
        index = itemListContext.positionInContainerSection.toDouble(),
        totalCount = itemListContext.sectionCount,
    )

    private fun handleCopy(vaultItem: VaultItem<*>, copyField: CopyField): Boolean {
        if (!vaultItemFieldContentService.hasContent(vaultItem, copyField)) {
            return false
        }

        val content = vaultItemFieldContentService.getContent(vaultItem, copyField) ?: return false
        val summaryObject: SummaryObject = vaultItem.toSummary()

        clipboardCopy.copyToClipboard(
            data = content,
            sensitiveData = copyField.isSensitiveData,
            autoClear = true,
            feedback = copyField.getFeedback(),
        )
        vaultItemCopyListenerHolder.getVaultItemCopyListener().forEach { copyListener ->
            copyListener.onCopyFromVault(summaryObject = summaryObject, copyField = copyField)
        }

        vaultItemCopyLogger.sendLog(copyField = copyField, summaryObject = summaryObject)
        sessionCoroutineScope?.launch {
            markAsViewedAndUsed(summaryObject)
        }
        return true
    }

    override fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        updateFrequentSearch: Boolean,
        highlight: Highlight?,
        index: Double?,
        totalCount: Int?,
    ): Boolean {
        if (!vaultItemFieldContentService.hasContent(item, copyField)) {
            return false
        }
        val content = vaultItemFieldContentService.getContent(item, copyField) ?: return false

        if (updateFrequentSearch) {
            sessionCoroutineScope?.launch {
                frequentSearch.markedAsSearched(item.id, item.syncObjectType)
            }
        }

        clipboardCopy.copyToClipboard(
            data = content,
            sensitiveData = copyField.isSensitiveData,
            autoClear = true,
            feedback = copyField.getFeedback(),
        )
        vaultItemCopyListenerHolder.getVaultItemCopyListener().forEach {
            it.onCopyFromVault(
                summaryObject = item,
                copyField = copyField,
                highlight = highlight,
                index = index,
                totalCount = totalCount,
            )
        }

        vaultItemCopyLogger.sendLog(copyField = copyField, summaryObject = item)
        sessionCoroutineScope?.launch {
            markAsViewedAndUsed(item)
        }
        return true
    }

    private suspend fun markAsViewedAndUsed(item: SummaryObject) {
        val vaultItem = vaultDataQuery.query(
            vaultFilter {
                specificUid(item.id)
            },
        )?.copyWithAttrs {
            locallyViewedDate = Instant.now()
            locallyUsedCount += 1
        } ?: return
        val itemToSave = if (item.syncObjectType == SyncObjectType.AUTHENTIFIANT) {
            vaultItem
                .asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
                ?.copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }?.copySyncObject {
                    lastUse = Instant.now()
                }
        } else {
            vaultItem
        } ?: return
        dataSaver.save(itemToSave)
    }

    private fun getSyncObjectFromVault(itemId: String) =
        vaultDataQuery.queryLegacy(
            vaultFilter {
                specificUid(itemId)
            },
        )

    override fun hasContent(item: SummaryObject, copyField: CopyField): Boolean = when {
        item.sharingPermission == UserPermission.LIMITED ||
            frozenStateManager.isAccountFrozen -> false
        else -> vaultItemFieldContentService.hasContent(item, copyField)
    }
}