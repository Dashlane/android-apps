package com.dashlane.wear.hilt

import android.content.Context
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors

@Module
@InstallIn(ActivityRetainedComponent::class)
object WearableNodeClientModule {
    @Provides
    fun providesRemoteActivityHelper(
        @ApplicationContext context: Context,
    ): RemoteActivityHelper {
        return RemoteActivityHelper(context, Executors.newSingleThreadExecutor())
    }

    @Provides
    fun providesCapabilityClient(
        @ApplicationContext context: Context,
    ): CapabilityClient {
        return Wearable.getCapabilityClient(context)
    }

    @Provides
    fun providesNodesClient(
        @ApplicationContext context: Context,
    ): NodeClient {
        return Wearable.getNodeClient(context)
    }
}