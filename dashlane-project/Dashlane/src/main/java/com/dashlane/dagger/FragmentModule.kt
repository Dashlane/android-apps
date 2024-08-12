package com.dashlane.dagger

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.dashlane.utils.coroutines.inject.qualifiers.FragmentLifecycleCoroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    @FragmentLifecycleCoroutineScope
    fun provideLifecycleCoroutineScope(fragment: Fragment): LifecycleCoroutineScope =
        fragment.lifecycleScope
}