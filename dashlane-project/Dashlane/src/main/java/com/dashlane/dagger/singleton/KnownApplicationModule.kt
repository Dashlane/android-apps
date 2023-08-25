package com.dashlane.dagger.singleton

import android.content.Context
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.ext.application.WhitelistApplication
import com.dashlane.ext.application.WhitelistApplicationImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object KnownApplicationModule {
    @Provides
    fun provideWhiteListApplication(@ApplicationContext context: Context): WhitelistApplication =
        WhitelistApplicationImpl(context)

    @Provides
    fun provideKnownApplicationProvider(@ApplicationContext context: Context): KnownApplicationProvider =
        KnownApplicationProvider(provideWhiteListApplication(context))
}