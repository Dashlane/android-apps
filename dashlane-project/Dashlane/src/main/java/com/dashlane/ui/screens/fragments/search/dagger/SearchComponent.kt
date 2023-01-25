package com.dashlane.ui.screens.fragments.search.dagger

import android.content.Context
import com.dashlane.ui.screens.fragments.search.SearchService

interface SearchComponent {
    val searchService: SearchService

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as SearchApplication).component
    }
}