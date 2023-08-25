package com.dashlane.dagger.singleton

import com.dashlane.ext.application.KnownApplicationProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface KnownApplicationEntryPoint {
    val knownApplicationProvider: KnownApplicationProvider
}