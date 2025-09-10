package com.dashlane.passwordhealth.identitydashboard.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
class JsonModule {
    @Provides
    fun providesJsonForBreachLoader(): Json =
        Json {
            
            coerceInputValues = true
        }
}