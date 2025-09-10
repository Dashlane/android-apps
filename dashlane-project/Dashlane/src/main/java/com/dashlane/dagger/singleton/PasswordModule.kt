package com.dashlane.dagger.singleton

import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.passwordstrength.PasswordStrengthCache
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.cache
import com.dashlane.passwordstrength.passwordStrengthEvaluator
import com.dashlane.util.inject.qualifiers.Cache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PasswordModule {
    @Provides
    @Cache
    fun providePasswordStrengthEvaluatorCached(
        evaluator: PasswordStrengthEvaluator,
        cache: PasswordStrengthCache,
    ): PasswordStrengthEvaluator =
        evaluator.cache(cache)

    @Provides
    fun providePasswordStrengthEvaluator(): PasswordStrengthEvaluator = passwordStrengthEvaluator()

    @Provides
    @Singleton
    fun providePasswordStrengthCache(): PasswordStrengthCache =
        PasswordStrengthCache() 

    @Provides
    fun providePasswordGenerator(): PasswordGenerator =
        PasswordGenerator()
}