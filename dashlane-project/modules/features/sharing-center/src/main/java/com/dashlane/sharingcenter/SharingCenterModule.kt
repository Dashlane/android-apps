package com.dashlane.sharingcenter

import android.content.res.Resources
import arrow.core.partially1
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.session.SessionManager
import com.dashlane.sharing.SharingDataProvider
import com.dashlane.sharingcenter.contact.SharingContactCreator
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.vault.item.VaultListItemStateFactory
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
object SharingCenterModule {
    @Suppress("LongParameterList", "kotlin:S107")
    @Provides
    @ViewModelScoped
    fun provideSharingCenterUseCases(
        currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
        sharingDataProvider: SharingDataProvider,
        sharingContactCreator: SharingContactCreator,
        vaultListItemStateFactory: VaultListItemStateFactory,
        frozenStateManager: FrozenStateManager,
        dataSync: DataSync,
        sessionManager: SessionManager,
        resources: Resources,
    ): MutableList<UseCase<SharingCenterState.View, SharingCenterEvent>> =
        mutableListOf(
            ::load
                .partially1(currentTeamSpaceUiFilter)
                .partially1(sharingDataProvider)
                .partially1(sharingContactCreator)
                .partially1(vaultListItemStateFactory)
                .partially1(sessionManager)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processAcceptCollectionInvite
                .partially1(frozenStateManager)
                .partially1(sharingDataProvider)
                .partially1(dataSync)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processDeclineCollectionInvite
                .partially1(sharingDataProvider)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processAcceptGroupInvite
                .partially1(frozenStateManager)
                .partially1(sharingDataProvider)
                .partially1(dataSync)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processDeclineGroupInvite
                .partially1(sharingDataProvider)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processAcceptItemInvite
                .partially1(frozenStateManager)
                .partially1(sharingDataProvider)
                .partially1(dataSync)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::processDeclineItemInvite
                .partially1(sharingDataProvider)
                .partially1(resources)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
}