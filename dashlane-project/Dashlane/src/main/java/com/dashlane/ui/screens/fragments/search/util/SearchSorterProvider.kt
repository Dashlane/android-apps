package com.dashlane.ui.screens.fragments.search.util

import android.content.Context
import com.dashlane.loaders.datalists.search.RankingSearchSorter
import com.dashlane.loaders.datalists.search.SearchImprovementsUtils
import com.dashlane.search.SearchSorter
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.vault.util.IdentityNameHolderService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SearchSorterProvider @Inject constructor(@ApplicationContext val context: Context) {
    fun getSearchSorter(
        textResolver: DataIdentifierListTextResolver,
        identityNameHolderService: IdentityNameHolderService
    ): SearchSorter =
        RankingSearchSorter(
            context,
            SearchImprovementsUtils(textResolver, identityNameHolderService)
        )
}