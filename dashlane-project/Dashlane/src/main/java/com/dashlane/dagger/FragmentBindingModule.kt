package com.dashlane.dagger

import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(FragmentComponent::class)
interface FragmentBindingModule {
    @Binds
    @FragmentLifecycleCoroutineScope
    fun bindLifecycleCoroutineScope(@FragmentLifecycleCoroutineScope coroutineScope: LifecycleCoroutineScope): CoroutineScope
}