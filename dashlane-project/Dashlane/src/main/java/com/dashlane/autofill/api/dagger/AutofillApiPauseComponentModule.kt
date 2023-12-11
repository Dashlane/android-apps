package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.PreferencesPausedFormSourcesRepository
import com.dashlane.autofill.pause.services.MutexPausedFormSourcesProvider
import com.dashlane.autofill.pause.services.PausedFormSourcesProvider
import com.dashlane.autofill.pause.services.PausedFormSourcesRepository
import com.dashlane.autofill.pause.services.PausedFormSourcesStrings
import com.dashlane.autofill.pause.services.PausedFormSourcesStringsRepository
import com.dashlane.autofill.pause.services.RemovePauseContract
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
internal abstract class AutofillApiPauseComponentModule {
    @Singleton
    @Binds
    abstract fun bindsPausedFormSourcesRepository(impl: PreferencesPausedFormSourcesRepository): PausedFormSourcesRepository

    @Singleton
    @Binds
    abstract fun bindsPausedFormSourcesProvider(impl: MutexPausedFormSourcesProvider): PausedFormSourcesProvider

    @Singleton
    @Binds
    abstract fun bindsUnpausingContract(impl: MutexPausedFormSourcesProvider): RemovePauseContract

    @Singleton
    @Binds
    abstract fun bindsPausedFormSourcesStringsRepository(impl: PausedFormSourcesStrings): PausedFormSourcesStringsRepository
}
