package com.dashlane.autofill.api.changepause.dagger

import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.changepause.model.ChangePauseDataProvider
import com.dashlane.autofill.api.changepause.presenter.ChangePausePresenter
import com.dashlane.autofill.api.changepause.view.ChangePauseViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent



@Module
@InstallIn(ActivityComponent::class)
abstract class ChangePauseViewModelModule {
    @ChangePauseViewModelScope
    @Binds
    abstract fun bindsChangePauseContractDataProvider(impl: ChangePauseDataProvider): ChangePauseContract.DataProvider

    @ChangePauseViewModelScope
    @Binds
    abstract fun bindsChangePauseContractPresenter(impl: ChangePausePresenter): ChangePauseContract.Presenter
}
