package com.dashlane.login.dagger

import androidx.fragment.app.FragmentActivity
import com.dashlane.login.TrackingIdProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class LoginCreateAccountSharedModule {
    companion object {
        @Provides
        @TrackingId
        fun getTrackingId(fragmentActivity: FragmentActivity): String = TrackingIdProvider.getOrGenerateTrackingId(fragmentActivity)
    }
}