package com.dashlane.ui.screens.settings.item

import android.content.Context



interface SettingLoadable {

    

    fun isLoaded(context: Context): Boolean
}