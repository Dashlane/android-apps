package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

interface HeaderProvider {
    fun getHeaderFor(context: Context, viewTypeProvider: DashlaneRecyclerAdapter.ViewTypeProvider): String?
}