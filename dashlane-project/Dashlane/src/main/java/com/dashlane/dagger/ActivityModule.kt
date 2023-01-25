package com.dashlane.dagger

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.dashlane.util.inject.qualifiers.ActivityLifecycleCoroutineScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {
    @Provides
    fun provideLayoutInflater(activity: Activity): LayoutInflater =
        activity.layoutInflater

    @Provides
    fun provideIntent(activity: Activity): Intent =
        activity.intent

    @Provides
    @ActivityLifecycleCoroutineScope
    fun provideLifecycleCoroutineScope(activity: FragmentActivity): LifecycleCoroutineScope =
        activity.lifecycleScope
}