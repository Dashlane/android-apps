package com.dashlane.premium.paywall.dagger

import com.dashlane.premium.paywall.common.PaywallIntroFactory
import com.dashlane.premium.paywall.common.PaywallIntroFactoryImpl
import com.dashlane.premium.paywall.common.PaywallLogger
import com.dashlane.premium.paywall.common.PaywallLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
interface PaywallModule {

    @Binds
    fun bindPaywallIntroFactory(impl: PaywallIntroFactoryImpl): PaywallIntroFactory

    @Binds
    fun bindPaywallLogger(impl: PaywallLoggerImpl): PaywallLogger
}