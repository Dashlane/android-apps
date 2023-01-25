package com.dashlane.autofill.api

import android.content.Context
import com.dashlane.autofill.api.internal.FetchAccounts
import com.dashlane.loaders.datalists.SearchLoader
import com.dashlane.loaders.datalists.search.RankingSearchSorter
import com.dashlane.loaders.datalists.search.SearchImprovementsUtils
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject



class FetchAccountsUsingLoader @Inject constructor(
    @ApplicationContext
    val applicationContext: Context,
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    identityUtil: IdentityUtil,
    userFeaturesChecker: UserFeaturesChecker
) : FetchAccounts {
    private val searchLoader: SearchLoader

    init {
        val fieldMatcher = SearchImprovementsUtils(DataIdentifierListTextResolver(identityUtil), identityUtil, userFeaturesChecker)
        searchLoader = SearchLoader(
            searchSorter = RankingSearchSorter(applicationContext, fieldMatcher),
            scope = globalCoroutineScope
        )
    }

    

    override fun fetchAccount(authentifiantIds: List<String>): List<SummaryObject.Authentifiant>? =
        searchLoader.loadAuthentifiantsById(authentifiantIds)
}
