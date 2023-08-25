package com.dashlane.ui.screens.settings.item

import android.content.Context

interface SettingCheckable {

    fun isChecked(context: Context): Boolean

    fun onCheckChanged(context: Context, enable: Boolean)
}