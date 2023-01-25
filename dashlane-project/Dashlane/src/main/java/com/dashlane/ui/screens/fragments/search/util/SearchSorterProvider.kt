package com.dashlane.ui.screens.fragments.search.util

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.loaders.datalists.search.RankingSearchSorter
import com.dashlane.loaders.datalists.search.SearchImprovementsUtils
import com.dashlane.search.SearchSorter
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.util.IdentityUtil

object SearchSorterProvider {
    fun getSearchSorter(
        textResolver: DataIdentifierListTextResolver,
        identityUtil: IdentityUtil,
        userFeatureChecker: UserFeaturesChecker
    ): SearchSorter =
        RankingSearchSorter(
            SingletonProvider.getContext(),
            SearchImprovementsUtils(textResolver, identityUtil, userFeatureChecker)
        )
}