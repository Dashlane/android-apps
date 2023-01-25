package com.dashlane.dagger

import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.ActivityLifecycleCoroutineScope
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ActivityComponent::class)
interface ActivityBindingModule {
    @Binds
    @ActivityLifecycleCoroutineScope
    fun bindLifecycleCoroutineScope(@ActivityLifecycleCoroutineScope coroutineScope: LifecycleCoroutineScope): CoroutineScope
}