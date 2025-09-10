package com.dashlane.vpn.thirdparty

import arrow.core.partially1
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class)
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object VpnThirdPartyModule {
    @Provides
    @ViewModelScoped
    fun provideUseCases(
        repository: VpnThirdPartyRepository,
        preferences: GlobalPreferencesManager,
        logger: VpnThirdPartyLogger,
        clipboardCopy: ClipboardCopy,
    ): MutableList<UseCase<VpnThirdPartyState.View, VpnThirdPartyEvent>> {
        return mutableListOf<UseCase<VpnThirdPartyState.View, VpnThirdPartyEvent>>(
            ::getHotspotShieldAccount
                .partially1(repository)
                .partially1(preferences)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::copyLogin
                .partially1(clipboardCopy)
                .partially1(logger)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::copyPassword
                .partially1(clipboardCopy)
                .partially1(logger)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::logDownloadAppClick
                .partially1(repository)
                .partially1(logger)
                .toFilteredUseCase(Strategy.DropOnNewState),
            ::getLaunchHotspotShieldIntent
                .partially1(repository)
                .toFilteredUseCase(Strategy.DropOnNewState),
        )
    }
}