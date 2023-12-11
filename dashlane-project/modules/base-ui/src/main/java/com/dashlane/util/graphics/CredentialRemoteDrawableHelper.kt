package com.dashlane.util.graphics

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dashlane.url.icon.UrlDomainIcon
import com.dashlane.url.icon.UrlDomainIconException
import com.dashlane.url.icon.toColorIntOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal fun <T> Context.launchCollect(flow: Flow<T>, callback: (T) -> Unit) {
    this.asLifecycleOwner()?.lifecycleScope?.launch {
        try {
            flow.collect {
                callback(it)
            }
        } catch (e: UrlDomainIconException) {
        }
    }
}

internal fun Context.asLifecycleOwner(): LifecycleOwner? =
    when (this) {
        is LifecycleOwner -> this
        is ContextWrapper -> this.baseContext.asLifecycleOwner()
        else -> {
            null
        }
    }

@get:ColorInt
val UrlDomainIcon.backgroundColor: Int?
    get() = colors.background.toColorIntOrNull()
