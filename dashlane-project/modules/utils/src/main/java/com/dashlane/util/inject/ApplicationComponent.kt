package com.dashlane.util.inject

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext



interface ApplicationComponent {

    @get:ApplicationContext
    val applicationContext: Context

    companion object {

        

        operator fun invoke(context: Context) =
            (context.applicationContext as ComponentApplication).component
    }
}
