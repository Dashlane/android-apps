package com.dashlane.autofill.actionssources.view

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.autofill.actionssources.model.ActionedFormSource
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

interface AutofillFormSourceViewTypeProviderFactory {
    fun create(actionedFormSource: ActionedFormSource): AutofillFormSourceWrapper
    interface AutofillFormSourceWrapper : DashlaneRecyclerAdapter.ViewTypeProvider {
        val title: String
        val type: String

        fun buildDrawable(context: Context): Drawable
    }
}
