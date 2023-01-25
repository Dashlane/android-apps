package com.dashlane.sync

import com.dashlane.cryptography.CryptographyComponent
import com.dashlane.cryptography.SharingCryptographyComponent
import com.dashlane.server.api.dagger.DashlaneApiComponent
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule
import com.dashlane.sharing.SharingKeysHelperComponent
import com.dashlane.sync.cryptochanger.SyncCryptoChanger
import com.dashlane.sync.repositories.SyncRepository
import com.dashlane.sync.sharing.SharingComponent
import dagger.Component

@SyncScope
@Component(
    modules = [
        SyncBindingModule::class,
        SyncLogsModule::class,
        DashlaneApiEndpointsModule::class
    ],
    dependencies = [
        DashlaneApiComponent::class,
        CryptographyComponent::class,
        SharingCryptographyComponent::class,
        SharingComponent::class,
        SharingKeysHelperComponent::class
    ]
)
interface SyncComponent {
    val syncRepository: SyncRepository
    val syncCryptoChanger: SyncCryptoChanger
}
