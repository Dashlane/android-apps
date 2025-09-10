package com.dashlane.pin.settings.success

import arrow.core.partially1
import com.dashlane.pin.PinSetupRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object PinSettingSuccessModule {
    @Provides
    @ViewModelScoped
    fun providePinSettingSuccessUseCases(
        sessionManager: SessionManager,
        sessionCredentialsSaver: SessionCredentialsSaver,
        pinSetupRepository: PinSetupRepository,
    ): MutableList<UseCase<PinSettingsSuccessState.View, PinSettingsSuccessEvent>> {
        return mutableListOf(
            ::checkCredentialAreSaved
                .partially1(sessionManager)
                .partially1(sessionCredentialsSaver)
                .partially1(pinSetupRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::savePin
                .partially1(sessionManager)
                .partially1(pinSetupRepository)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}