package com.dashlane.util.clipboard

import com.dashlane.util.clipboard.vault.VaultItemClipboard
import com.dashlane.util.clipboard.vault.VaultItemClipboardImpl
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.util.clipboard.vault.VaultItemFieldContentServiceImpl
import dagger.Binds
import dagger.Module

@Module
abstract class CopyComponentModule {
    @Binds
    abstract fun bindVaultItemClipboardImpl(impl: VaultItemClipboardImpl): VaultItemClipboard

    @Binds
    abstract fun bindVaultItemFieldContentService(impl: VaultItemFieldContentServiceImpl): VaultItemFieldContentService
}