package com.dashlane.dagger.singleton

import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(
    includes = [
        DashlaneApiEndpointsModule::class,
    ],
)
@InstallIn(SingletonComponent::class)
object SingletonAggregateModule