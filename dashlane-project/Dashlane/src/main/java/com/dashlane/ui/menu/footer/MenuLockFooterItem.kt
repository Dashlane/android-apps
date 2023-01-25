package com.dashlane.ui.menu.footer

import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.menu.MenuDef
import com.dashlane.ui.menu.MenuUsageLogger
import com.dashlane.ui.menu.item.MenuItemViewHolder



class MenuLockFooterItem : MenuDef.Item {
    val type = DashlaneRecyclerAdapter.ViewType(
        R.layout.item_menu_lock_footer,
        MenuItemViewHolder::class.java
    )

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = type
    override fun doNavigation(menuUsageLogger: MenuUsageLogger) {
        menuUsageLogger.logMenuLock()
        SingletonProvider.getLockManager().lock()
    }
}