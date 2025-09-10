package com.dashlane.wear.hilt

import android.content.Context
import android.os.Bundle
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.debug.services.DaDaDaFeatures
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.debug.services.DaDaDaSecurity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
interface WearDebugModule {
    @Binds
    fun bindsDaDaDaLogin(impl: WearDaDaDaStub): DaDaDaLogin

    @Binds
    fun bindsDaDaDaSecurity(impl: WearDaDaDaStub): DaDaDaSecurity

    @Binds
    fun bindsDadDaDaFeatures(impl: WearDaDaDaStub): DaDaDaFeatures

    @Binds
    fun bindsBaseDaDaDa(impl: WearDaDaDaStub): DaDaDaBase
}

class WearDaDaDaStub @Inject constructor() : DaDaDaLogin, DaDaDaSecurity, DaDaDaFeatures {
    override val deepLink: String? = null
    override val loginTemplate: String? = null
    override val defaultPassword: String? = null
    override val isSkipOnboardingPostAccountCreation: Boolean = false
    override val isScreenshotAllowed: Boolean = false
    override val isCreateAccountWithRemoteKeyEnabled: Boolean = false
    override val isCreateAccountWithLegacyCryptoEnabled: Boolean = false
    override val dataFlow: SharedFlow<Bundle> = MutableSharedFlow<Bundle>().asSharedFlow()
    override val featureFlippingString: List<String> = emptyList()
    override val isEnabled: Boolean = false
    override val isLeakCanaryEnabled: Boolean = false
    override val playStoreTestAccounts = emptyList<String>()

    override fun clearShowAllSecurityWarnings() = Unit

    override fun triggerCrashes() = Unit

    override fun hasDeepLink(): Boolean = false

    override fun fastLogin(username: String, password: String) = Unit

    override fun getSecurityToken(username: String): Flow<String> = emptyFlow()

    override fun refreshAsync(context: Context) = Unit

    override fun clearData() = Unit

    override fun updateData(extras: Bundle) = Unit

    override fun generateUsername(): String = ""
}