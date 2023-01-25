package com.dashlane.ui.screens.settings.item

import android.content.Context

interface SettingItem {

    

    val id: String
    

    val header: SettingHeader?
    

    val title: String
    

    val description: String?

    

    fun isEnable(context: Context): Boolean

    

    fun isVisible(context: Context): Boolean

    

    fun onClick(context: Context)
}
