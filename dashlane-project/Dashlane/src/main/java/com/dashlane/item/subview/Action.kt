package com.dashlane.item.subview

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity



interface Action {

    @get:StringRes
    val text: Int

    @get:DrawableRes
    val icon: Int

    @get:AttrRes
    val tintColorRes: Int?

    fun onClickAction(activity: AppCompatActivity)
}