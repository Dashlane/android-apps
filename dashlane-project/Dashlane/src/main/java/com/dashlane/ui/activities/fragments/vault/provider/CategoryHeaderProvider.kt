package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.activities.fragments.vault.VaultItemViewTypeProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.xml.domain.SyncObjectType

class CategoryHeaderProvider(private val categoriesTitles: Map<SyncObjectType, String?>) : HeaderProvider {
    override fun getHeaderFor(context: Context, viewTypeProvider: ViewTypeProvider): String? {
        if (viewTypeProvider !is VaultItemViewTypeProvider) {
            return null
        }
        val catName = categoriesTitles[viewTypeProvider.summaryObject.syncObjectType]
        return catName ?: context.getString(R.string.unspecified_category)
    }
}