package com.dashlane.wear.hilt

import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.authentication.LoginDataReset
import com.dashlane.authentication.LoginDataResetImpl
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.UserStorageImpl
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.featureflipping.UserFeaturesCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface WearUserModule {
    @Binds
    fun bindsLoginDataReset(impl: LoginDataResetImpl): LoginDataReset

    @Binds
    fun bindsUserStorage(impl: UserStorageImpl): UserStorage

    @Binds
    fun bindsUserAccountStorage(impl: UserAccountStorageImpl): UserAccountStorage

    @Binds
    @Singleton
    fun bindsUserFeatureChecker(impl: UserFeaturesCheckerImpl): UserFeaturesChecker
}