package com.dashlane.passkeys.hilt

import com.dashlane.passkeys.PasskeysRestrictionsCheckerImpl
import com.dashlane.storage.userdata.restriction.PasskeysRestrictionsChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
fun interface PasskeyModule {
    @Binds
    fun bindsPasskeysRestrictionChecker(impl: PasskeysRestrictionsCheckerImpl): PasskeysRestrictionsChecker
}