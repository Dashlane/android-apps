package com.dashlane.item.subview.action

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.item.subview.Action



open class MenuAction(
    override val text: Int,
    override val icon: Int,
    val displayFlags: Int,
    val checkable: Boolean = false,
    val checked: Boolean = false,
    private val action: (Activity) -> Unit = {}
) : Action {

    override val tintColorRes: Int? = null

    override fun onClickAction(activity: AppCompatActivity) {
        action.invoke(activity)
    }
}