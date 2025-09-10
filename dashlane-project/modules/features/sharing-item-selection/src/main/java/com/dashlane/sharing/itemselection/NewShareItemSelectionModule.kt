package com.dashlane.sharing.itemselection

import arrow.core.partially1
import com.dashlane.lock.LockManager
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import com.dashlane.vault.item.VaultListItemStateFactory
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object NewShareItemSelectionModule {
    @Provides
    @ViewModelScoped
    fun provideNewShareItemSelectionUseCases(
        newShareItemsRepository: NewShareItemsRepository,
        vaultListItemStateFactory: VaultListItemStateFactory,
        identityNameHolderService: IdentityNameHolderService,
        lockManager: LockManager,
    ): MutableList<UseCase<NewShareItemSelectionState.View, NewShareItemSelectionEvent>> {
        return mutableListOf(
            ::loadShareableItems
                .partially1(newShareItemsRepository)
                .partially1(vaultListItemStateFactory)
                .partially1(identityNameHolderService)
                .toFilteredUseCase(Strategy.Debounce(200.milliseconds)),
            ::toggleItem
                .partially1(newShareItemsRepository)
                .partially1(vaultListItemStateFactory)
                .partially1(identityNameHolderService)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::checkProtectedItems
                .partially1(newShareItemsRepository)
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}