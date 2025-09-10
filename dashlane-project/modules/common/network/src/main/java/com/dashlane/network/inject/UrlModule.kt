package com.dashlane.network.inject

import com.dashlane.network.webservices.DashlaneUrls
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
class UrlModule {
    @Provides
    @BaseUrl
    fun provideBaseUrl() = DashlaneUrls.URL_API
}