package com.dashlane.ui.menu.item

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.menu.MenuDef
import com.dashlane.ui.menu.MenuUsageLogger



open class MenuItem @JvmOverloads constructor(
    @DrawableRes val iconResId: Int,
    @DrawableRes val iconSelectedResId: Int,
    @StringRes val titleResId: Int,
    val destinationResIds: Array<Int>? = null,
    endIconProvider: MenuItemEndIconProvider = MenuItemEndIconProvider.None,
    premiumTagProvider: MenuItemPremiumTagProvider = MenuItemPremiumTagProvider.None,
    val callback: () -> Unit
) : MenuDef.Item,
    MenuItemEndIconProvider by endIconProvider,
    MenuItemPremiumTagProvider by premiumTagProvider {

    sealed class PremiumTag {
        object PremiumOnly : PremiumTag()
        data class Trial(val remainingDays: Long) : PremiumTag()
        object None : PremiumTag()
    }

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = VIEW_TYPE

    override fun doNavigation(menuUsageLogger: MenuUsageLogger) {
        callback.invoke()
    }

    companion object {
        private val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_menu_item,
            MenuItemViewHolder::class.java
        )
    }
}

interface MenuItemEndIconProvider {
    fun getEndIcon(context: Context): Drawable?
    fun getEndIconDescription(context: Context): String?

    object None : MenuItemEndIconProvider {
        override fun getEndIcon(context: Context): Drawable? = null
        override fun getEndIconDescription(context: Context): String? = null
    }
}

interface MenuItemPremiumTagProvider {
    val premiumTag: MenuItem.PremiumTag

    object None : MenuItemPremiumTagProvider {
        override val premiumTag get(): MenuItem.PremiumTag = MenuItem.PremiumTag.None
    }
}