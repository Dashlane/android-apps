package com.dashlane.autofill.api.viewallaccounts.dagger

import com.dashlane.autofill.api.viewallaccounts.model.AuthentifiantsSearchAndFilterDataProvider
import com.dashlane.autofill.api.viewallaccounts.model.AutofillSearch
import com.dashlane.autofill.api.viewallaccounts.presenter.AuthentifiantsSearchAndFilterPresenter
import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.autofill.api.viewallaccounts.view.BottomSheetAuthentifiantsSearchAndFilterDialogFragment
import com.dashlane.autofill.api.viewallaccounts.view.BottomSheetAuthentifiantsSearchAndFilterViewProxy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope



@Module
@InstallIn(ActivityComponent::class)
class BottomSheetViewAllItemsModule {

    @BottomSheetViewAllItemsScope
    @Provides
    fun providesAuthentifiantsSearchAndFilterDataProvider(
        autofillSearch: AutofillSearch
    ): AuthentifiantsSearchAndFilterDataProvider {
        return AuthentifiantsSearchAndFilterDataProvider(
            autofillSearch
        )
    }

    @BottomSheetViewAllItemsScope
    @Provides
    fun providesAuthentifiantsSearchAndFilterPresenter(
        dataProvider: AuthentifiantsSearchAndFilterDataProvider
    ): AuthentifiantsSearchAndFilterPresenter {
        return AuthentifiantsSearchAndFilterPresenter(dataProvider)
    }

    @BottomSheetViewAllItemsScope
    @Provides
    fun providesBottomSheetAuthentifiantsSearchAndFilterViewProxy(
        bottomSheetDialogFragment: BottomSheetAuthentifiantsSearchAndFilterDialogFragment,
        authentifiantSearchViewTypeProviderFactory: AuthentifiantSearchViewTypeProviderFactory,
        presenter: AuthentifiantsSearchAndFilterPresenter
    ): BottomSheetAuthentifiantsSearchAndFilterViewProxy {
        val view = BottomSheetAuthentifiantsSearchAndFilterViewProxy(
            bottomSheetDialogFragment,
            authentifiantSearchViewTypeProviderFactory,
            presenter
        )
        presenter.setView(view, bottomSheetDialogFragment.activity as CoroutineScope)

        return view
    }
}
