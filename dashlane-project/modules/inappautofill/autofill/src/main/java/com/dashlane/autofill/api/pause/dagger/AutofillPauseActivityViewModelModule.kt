package com.dashlane.autofill.api.pause.dagger

import com.dashlane.autofill.api.pause.services.PausedFormSourcesProvider
import com.dashlane.autofill.api.pause.services.PausedFormSourcesStringsRepository
import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.api.pause.model.AskPauseDataProvider
import com.dashlane.autofill.api.pause.presenter.AskPausePresenter
import com.dashlane.autofill.api.pause.view.AutofillPauseActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named



@Module
@InstallIn(ActivityComponent::class)
class AutofillPauseActivityViewModelModule {
    @AutofillPauseActivityViewModelScope
    @Provides
    fun providesAutofillListPausesDataProvider(
        pausedFormSourcesProvider: PausedFormSourcesProvider,
        pausedFormSourcesStringsRepository: PausedFormSourcesStringsRepository,
        @Named("openInDashlane") openInDashlane: Boolean
    ): AskPauseContract.DataProvider {
        return AskPauseDataProvider(
            pausedFormSourcesProvider,
            pausedFormSourcesStringsRepository,
            openInDashlane
        )
    }

    @AutofillPauseActivityViewModelScope
    @Provides
    fun providesAutofillListPausesPresenter(
        dataProvider: AskPauseContract.DataProvider
    ): AskPauseContract.Presenter {
        return AskPausePresenter(
            dataProvider
        )
    }
}
