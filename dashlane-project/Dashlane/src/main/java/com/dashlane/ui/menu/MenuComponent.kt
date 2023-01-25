package com.dashlane.ui.menu

import android.content.Context
import com.dashlane.navigation.Navigator

interface MenuComponent {
    val menuPresenter: MenuDef.IPresenter
    val navigator: Navigator

    interface Application {
        val component: MenuComponent
    }

    companion object {
        operator fun invoke(context: Context) = (context.applicationContext as Application).component
    }
}