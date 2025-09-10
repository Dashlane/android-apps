package com.dashlane.premium.offer

import com.dashlane.premium.offer.common.OffersLogger
import com.dashlane.premium.offer.common.OffersLoggerImpl
import com.dashlane.premium.offer.common.ProductDetailsManager
import com.dashlane.premium.offer.common.ProductDetailsManagerImpl
import com.dashlane.premium.offer.common.StoreOffersFormatter
import com.dashlane.premium.offer.common.StoreOffersFormatterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OffersModule {
    @Singleton
    @Binds
    abstract fun bindProductDetailsManager(impl: ProductDetailsManagerImpl): ProductDetailsManager

    @Binds
    abstract fun bindOffersLogger(impl: OffersLoggerImpl): OffersLogger

    @Binds
    abstract fun bindStoreOffersFormatter(impl: StoreOffersFormatterImpl): StoreOffersFormatter
}