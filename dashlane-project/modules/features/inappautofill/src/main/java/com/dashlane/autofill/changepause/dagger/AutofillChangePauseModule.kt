package com.dashlane.autofill.changepause.dagger

import com.dashlane.autofill.changepause.ChangePauseContract
import com.dashlane.autofill.changepause.model.ChangePauseDataProvider
import com.dashlane.autofill.changepause.presenter.ChangePausePresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
abstract class AutofillChangePauseModule {
    @FragmentScoped
    @Binds
    abstract fun bindsChangePauseContractDataProvider(impl: ChangePauseDataProvider): ChangePauseContract.DataProvider

    @FragmentScoped
    @Binds
    abstract fun bindsChangePauseContractPresenter(impl: ChangePausePresenter): ChangePauseContract.Presenter
}
