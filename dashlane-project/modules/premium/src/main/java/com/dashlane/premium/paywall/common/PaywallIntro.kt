package com.dashlane.premium.paywall.common

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dashlane.hermes.generated.definitions.AnyPage

interface PaywallIntro {
    @get:DrawableRes
    val image: Int

    @get:StringRes
    val title: Int

    @get:StringRes
    val message: Int
    val messageFormatArgs: Array<Any>
    val trackingKey: String
    val page: AnyPage

    fun provideDetailsView(context: Context): View? = null
}
