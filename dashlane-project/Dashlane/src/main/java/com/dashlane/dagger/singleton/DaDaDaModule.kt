package com.dashlane.dagger.singleton

import com.dashlane.debug.DaDaDa
import com.dashlane.debug.services.DaDaDaAutofill
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.debug.services.DaDaDaBilling
import com.dashlane.debug.services.DaDaDaFeatures
import com.dashlane.debug.services.DaDaDaHermes
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.debug.services.DaDaDaSecurity
import com.dashlane.debug.services.DaDaDaTheme
import com.dashlane.debug.services.DaDaDaVersion
import com.dashlane.network.NitroUrlOverride
import com.dashlane.network.ServerUrlOverride
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DaDaDaModule {
    @Binds
    fun bindsBaseDadada(impl: DaDaDa): DaDaDaBase

    @Binds
    fun bindsAutofillCompanion(impl: DaDaDa): DaDaDaAutofill

    @Binds
    fun bindsBillingCompanion(impl: DaDaDa): DaDaDaBilling

    @Binds
    fun bindsFeaturesCompanion(impl: DaDaDa): DaDaDaFeatures

    @Binds
    fun bindsHermesCompanion(impl: DaDaDa): DaDaDaHermes

    @Binds
    fun bindsLoginCompanion(impl: DaDaDa): DaDaDaLogin

    @Binds
    fun bindsSecurityCompanion(impl: DaDaDa): DaDaDaSecurity

    @Binds
    fun bindsVersionCompanion(impl: DaDaDa): DaDaDaVersion

    @Binds
    fun bindsServerUrlOverride(impl: DaDaDa): ServerUrlOverride

    @Binds
    fun bindsNitroUrlOverride(impl: DaDaDa): NitroUrlOverride

    @Binds
    fun bindsThemeDaDaDa(impl: DaDaDa): DaDaDaTheme
}