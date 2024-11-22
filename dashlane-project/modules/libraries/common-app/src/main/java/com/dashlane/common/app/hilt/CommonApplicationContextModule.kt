package com.dashlane.common.app.hilt

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CommonApplicationContextModule {

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    @Provides
    fun provideTelephonyManager(@ApplicationContext context: Context): TelephonyManager? =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
}