package com.dashlane.autofill.api.actionssources.view

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.dashlane.autofill.api.actionssources.model.ActionedFormSource
import com.dashlane.autofill.api.actionssources.model.ActionedFormSourceIcon
import com.dashlane.ui.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.getImageDrawableByWebsiteUrl
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.graphics.RemoteImageDrawableWithDominantColorBorders
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

        override fun buildDrawable(context: Context): Drawable {
            return actionedFormSource.icon.getDrawable(context)
        }

        override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> {
            return DashlaneRecyclerAdapter.ViewType(
                R.layout.item_dataidentifier,
                AutofillFormSourceHolder::class.java
            )
        }

        private fun ActionedFormSourceIcon.getDrawable(context: Context): Drawable {
            return when (this) {
                is ActionedFormSourceIcon.InstalledApplicationIcon ->
                    getDrawableForInstalledApplication(context, this.applicationInfo)
                is ActionedFormSourceIcon.NotInstalledApplicationIcon -> getUnknownPlaceHolder(context)
                is ActionedFormSourceIcon.UrlIcon -> getDrawableForDomain(context, this.url)
                ActionedFormSourceIcon.IncorrectSignatureIcon -> getUnknownPlaceHolder(context)
            }
        }

        private fun getDrawableForInstalledApplication(
            context: Context,
            applicationInfo: ApplicationInfo
        ): Drawable {
            val installAppDrawable = context.packageManager.getApplicationIcon(applicationInfo)

            return prepareExternalAppDrawable(context, installAppDrawable)
        }

        private fun prepareExternalAppDrawable(
            context: Context,
            externalAppDrawable: Drawable
        ): Drawable {
            val drawable = RemoteImageDrawableWithDominantColorBorders(
                context,
                context.getThemeAttrColor(R.attr.colorPrimary)
            )
            drawable.setImage(externalAppDrawable, true)
            return drawable
        }

        private fun getUnknownPlaceHolder(context: Context): Drawable {
            return context.getImageDrawableByWebsiteUrl(null, null)
        }

        private fun getDrawableForDomain(context: Context, url: String): Drawable {
            return context.getImageDrawableByWebsiteUrl(url, url)
        }
    }
}
