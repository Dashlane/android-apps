package com.dashlane.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.util.inject.qualifiers.ViewModelCoroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelCoroutineScope
    fun provideCoroutineScope(viewModel: ViewModel): CoroutineScope =
        viewModel.viewModelScope
}