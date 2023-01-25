package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.activities.fragments.vault.VaultItemViewTypeProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.valueOfFromDataIdentifier

class CategoryHeaderProvider(val categoriesTitles: Map<String, String?>) : HeaderProvider {
    override fun getHeaderFor(context: Context, viewTypeProvider: ViewTypeProvider): String? {
        if (viewTypeProvider !is VaultItemViewTypeProvider) {
            return null
        }
        val categoryId = getCategoryId(viewTypeProvider.summaryObject)
        val catName = if (categoryId == null) {
            null
        } else {
            categoriesTitles[categoryId]
        }
        return catName ?: context.getString(R.string.unspecified_category)
    }

    private fun getCategoryId(summaryObject: SummaryObject): String? {
        return summaryObject.valueOfFromDataIdentifier()!!.getTableName()
    }
}