package com.dashlane.ui.activities.fragments.checklist.dagger

import com.dashlane.ui.activities.fragments.checklist.ChecklistDataProvider
import com.dashlane.ui.activities.fragments.checklist.ChecklistDataProviderContract
import com.dashlane.ui.activities.fragments.checklist.ChecklistLogger
import com.dashlane.ui.activities.fragments.checklist.ChecklistLoggerContract
import com.dashlane.ui.activities.fragments.checklist.ChecklistViewModel
import com.dashlane.ui.activities.fragments.checklist.ChecklistViewModelContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ChecklistModule {
    @Binds
    fun bindChecklistDataProvider(dataProvider: ChecklistDataProvider): ChecklistDataProviderContract

    @Binds
    fun bindChecklistLogger(logger: ChecklistLogger): ChecklistLoggerContract

    @Binds
    fun bindChecklistPresenter(viewModel: ChecklistViewModel): ChecklistViewModelContract
}