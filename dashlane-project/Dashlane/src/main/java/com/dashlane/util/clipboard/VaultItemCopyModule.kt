package com.dashlane.util.clipboard

import com.dashlane.followupnotification.services.FollowUpNotificationVaultItemCopyListenerImpl
import com.dashlane.util.clipboard.vault.VaultItemCopyListener
import com.dashlane.util.clipboard.vault.VaultItemCopyListenerHolder
import com.dashlane.util.clipboard.vault.VaultItemCopyListenerHolderByInjection
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.clipboard.vault.VaultItemCopyServiceImpl
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.util.clipboard.vault.VaultItemFieldContentServiceImpl
import com.dashlane.vault.VaultItemLogCopyListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object VaultItemCopyModule {

    @Provides
    fun providersVaultItemCopyService(impl: VaultItemCopyServiceImpl): VaultItemCopyService = impl

    @Provides
    fun providesVaultItemCopyListenerHolder(
        followUpNotificationVaultItemCopyListener: FollowUpNotificationVaultItemCopyListenerImpl,
        vaultItemLogCopyListener: VaultItemLogCopyListener
    ): VaultItemCopyListenerHolder {
        val list: MutableList<VaultItemCopyListener> = ArrayList()
        list.add(followUpNotificationVaultItemCopyListener)
        list.add(vaultItemLogCopyListener)
        return VaultItemCopyListenerHolderByInjection(list)
    }

    @Provides
    fun providesVaultItemFieldContentService(impl: VaultItemFieldContentServiceImpl): VaultItemFieldContentService {
        return impl
    }
}