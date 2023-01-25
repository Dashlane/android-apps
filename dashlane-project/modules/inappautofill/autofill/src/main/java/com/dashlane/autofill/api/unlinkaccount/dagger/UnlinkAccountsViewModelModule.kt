package com.dashlane.autofill.api.unlinkaccount.dagger

import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.autofill.api.unlinkaccount.model.UnlinkAccountsDataProvider
import com.dashlane.autofill.api.unlinkaccount.presenter.UnlinkAccountsPresenter
import com.dashlane.autofill.api.unlinkaccount.view.UnlinkAccountsViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent



@Module
@InstallIn(ActivityComponent::class)
abstract class UnlinkAccountsViewModelModule {
    @UnlinkAccountsViewModelScope
    @Binds
    abstract fun bindsUnlinkAccountsContractDataProvider(impl: UnlinkAccountsDataProvider): UnlinkAccountsContract.DataProvider

    @UnlinkAccountsViewModelScope
    @Binds
    abstract fun bindsUnlinkAccountsContractPresenter(impl: UnlinkAccountsPresenter): UnlinkAccountsContract.Presenter
}
