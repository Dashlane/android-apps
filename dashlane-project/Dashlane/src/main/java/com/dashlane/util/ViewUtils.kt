package com.dashlane.util

import android.view.View
import android.view.ViewParent



inline fun View.getRelativePosition(parent: ViewParent, accessor: (View) -> Float): Float {
    var c = 0.0f
    var v = this
    do {
        c += accessor(v)
        v = v.parent as View
    } while (v != parent)
    return c
}