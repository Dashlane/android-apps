package com.dashlane.ui.activities.fragments.list.action

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.dashlane.vault.summary.SummaryObject



interface ListItemAction {

    @get:DrawableRes
    val icon: Int

    @get:StringRes
    val contentDescription: Int

    @get:IdRes
    val viewId: Int
        get() = View.generateViewId()

    val visibility: Int

    fun onClickItemAction(v: View, item: SummaryObject)
}