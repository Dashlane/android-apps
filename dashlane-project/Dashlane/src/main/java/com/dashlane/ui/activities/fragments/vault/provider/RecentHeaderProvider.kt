package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.util.IdentityNameHolderService

object RecentHeaderProvider : HeaderProvider {
    override fun getHeaderFor(
        context: Context,
        viewTypeProvider: DashlaneRecyclerAdapter.ViewTypeProvider,
        identityNameHolderService: IdentityNameHolderService
    ) =
        context.getString(R.string.vault_header_most_recent)
}