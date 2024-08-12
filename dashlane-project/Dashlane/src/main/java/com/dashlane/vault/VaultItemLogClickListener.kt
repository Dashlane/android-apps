package com.dashlane.vault

import android.view.View
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.util.toHighlight
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter

class VaultItemLogClickListener<T>(
    private val vaultItemLogger: VaultItemLogger,
    private val delegate: EfficientAdapter.OnItemClickListener<T>,
    private val mapper: (item: T) -> VaultItemWrapper<*>?
) : EfficientAdapter.OnItemClickListener<T> {
    override fun onItemClick(
        adapter: EfficientAdapter<T>,
        view: View,
        item: T?,
        position: Int
    ) {
        item?.let(mapper)?.run {
            val highlight = itemListContext.section.toHighlight()
            
            val trackIndex =
                itemListContext.container == ItemListContext.Container.SEARCH || highlight != Highlight.NONE
            dataType?.toItemTypeOrNull()?.let {
                val id = summaryObject.anonymousId ?: return@run
                vaultItemLogger.logSelect(
                    highlight = itemListContext.section.toHighlight(),
                    itemId = id,
                    index = itemListContext.positionInContainerSection.takeIf { trackIndex },
                    itemType = it,
                    totalCount = itemListContext.sectionCount.takeIf { trackIndex }
                )
            }
        }

        delegate.onItemClick(adapter, view, item, position)
    }
}