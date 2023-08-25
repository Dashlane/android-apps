package com.dashlane.ui.menu.header

import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.menu.MenuDef
import com.dashlane.ui.menu.MenuUsageLogger

class MenuHeaderItem : MenuDef.Item {
    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> =
        DashlaneRecyclerAdapter.ViewType(
            R.layout.item_menu_header,
            MenuHeaderViewHolder::class.java
        )

    override fun doNavigation(menuUsageLogger: MenuUsageLogger) {
        
    }
}