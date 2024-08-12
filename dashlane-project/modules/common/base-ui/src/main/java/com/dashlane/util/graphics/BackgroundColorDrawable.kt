package com.dashlane.util.graphics

import androidx.annotation.ColorInt

interface BackgroundColorDrawable {
    @get:ColorInt
    @setparam:ColorInt
    var backgroundColor: Int
}