package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.util.IdentityNameHolderService

interface HeaderProvider {
    fun getHeaderFor(
        context: Context,
        viewTypeProvider: DashlaneRecyclerAdapter.ViewTypeProvider,
        identityNameHolderService: IdentityNameHolderService
    ): String?
}