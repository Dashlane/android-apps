package com.dashlane.dagger.singleton

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.session.SessionManager
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.userfeatures.UserFeaturesCheckerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class FeatureCheckerModule {
    @Provides
    @Singleton
    open fun provideUserFeatureChecker(
        sessionManager: SessionManager,
        accountStatusRepository: AccountStatusRepository,
        userFeature: FeatureFlipManager
    ): UserFeaturesChecker {
        return UserFeaturesCheckerImpl(sessionManager, accountStatusRepository, userFeature)
    }
}