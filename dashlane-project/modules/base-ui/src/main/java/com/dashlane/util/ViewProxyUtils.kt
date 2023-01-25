package com.dashlane.util

import android.view.View
import androidx.annotation.IdRes
import com.skocken.presentation.viewproxy.BaseViewProxy



inline fun BaseViewProxy<*>.onClick(@IdRes id: Int, crossinline action: (View?) -> Unit) {
    findViewByIdEfficient<View>(id)!!.setOnClickListener { action(it) }
}
