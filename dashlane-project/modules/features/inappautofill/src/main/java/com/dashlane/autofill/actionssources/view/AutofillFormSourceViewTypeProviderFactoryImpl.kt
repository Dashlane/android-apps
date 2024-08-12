package com.dashlane.autofill.actionssources.view

import android.content.Context
import android.graphics.drawable.Drawable
import com.dashlane.autofill.actionssources.model.ActionedFormSource
import com.dashlane.autofill.actionssources.model.ActionedFormSourceIcon
import com.dashlane.ui.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import javax.inject.Inject

class AutofillFormSourceViewTypeProviderFactoryImpl @Inject constructor() : AutofillFormSourceViewTypeProviderFactory {

    override fun create(actionedFormSource: ActionedFormSource): AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper {
        return AutoFillFormSourceWrapper(actionedFormSource)
    }

    private class AutoFillFormSourceWrapper(
        val actionedFormSource: ActionedFormSource
    ) : AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper {
        override val title: String
            get() = actionedFormSource.title
        override val type: String
            get() = actionedFormSource.type

        override fun getUrlDomain(): String? =
            if (actionedFormSource.icon is ActionedFormSourceIcon.UrlIcon) {
                actionedFormSource.icon.url
            } else {
                null
            }

        override fun isApp(): Boolean = (actionedFormSource.icon is ActionedFormSourceIcon.UrlIcon).not()

        override fun getAppDrawable(context: Context): Drawable? =
            if (actionedFormSource.icon is ActionedFormSourceIcon.InstalledApplicationIcon) {
                context.packageManager.getApplicationIcon(actionedFormSource.icon.applicationInfo)
            } else {
                null
            }

        override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> {
            return DashlaneRecyclerAdapter.ViewType(
                R.layout.item_dataidentifier,
                AutofillFormSourceHolder::class.java
            )
        }
    }
}
