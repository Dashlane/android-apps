package com.dashlane.ui.screens.settings.item

import android.content.Context
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator

data class SettingScreenItem(
    val navigator: Navigator,
    val page: AnyPage,
    val item: SettingItem,
    val subItems: List<SettingItem>
) : SettingItem by item {

    override fun onClick(context: Context) {
        navigator.goToSettings(item.id)
    }
}