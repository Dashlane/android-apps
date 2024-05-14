package com.dashlane.dagger.singleton

import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilterImpl
import com.dashlane.util.inject.OptionalProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TeamSpaceModule {
    @Binds
    @Singleton
    fun bindsTeamSpaceAccessorProvider(impl: TeamSpaceAccessorProvider): OptionalProvider<TeamSpaceAccessor>

    @Binds
    @Singleton
    fun bindCurrentTeamSpaceFilter(impl: CurrentTeamSpaceUiFilterImpl): CurrentTeamSpaceUiFilter
}