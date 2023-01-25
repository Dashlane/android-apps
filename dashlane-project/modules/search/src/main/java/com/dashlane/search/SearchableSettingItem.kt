package com.dashlane.search

import android.content.Context

interface SearchableSettingItem {
    fun getPathDisplay(): String

    fun onClick()

    fun getSettingId(): String

    fun getSettingTitle(): String

    fun getSettingDescription(): String?

    fun isSettingVisible(context: Context): Boolean
}