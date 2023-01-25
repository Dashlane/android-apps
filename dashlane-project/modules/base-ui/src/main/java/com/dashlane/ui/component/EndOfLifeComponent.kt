package com.dashlane.ui.component

import android.content.Context
import com.dashlane.ui.endoflife.EndOfLife

interface EndOfLifeComponent {
    val endOfLife: EndOfLife

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as EndOfLifeApplication).component
    }
}