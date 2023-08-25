package com.dashlane.autofill.api.pause.dagger

import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.api.pause.model.AskPauseDataProvider
import com.dashlane.autofill.api.pause.presenter.AskPausePresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
interface AutofillAskPauseModule {
    @FragmentScoped
    @Binds
    fun providesAutofillListPausesDataProvider(impl: AskPauseDataProvider): AskPauseContract.DataProvider

    @FragmentScoped
    @Binds
    fun providesAutofillListPausesPresenter(impl: AskPausePresenter): AskPauseContract.Presenter
}
