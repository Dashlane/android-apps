package com.dashlane.premium.paywall.dagger

import com.dashlane.premium.paywall.common.PaywallLogger
import com.dashlane.premium.paywall.common.PaywallLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface PaywallModule {

    @Binds
    fun bindPaywallLogger(impl: PaywallLoggerImpl): PaywallLogger
}