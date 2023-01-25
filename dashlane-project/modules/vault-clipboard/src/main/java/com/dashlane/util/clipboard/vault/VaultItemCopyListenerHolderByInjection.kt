package com.dashlane.util.clipboard.vault

import javax.inject.Inject



class VaultItemCopyListenerHolderByInjection @Inject constructor(
    private val vaultItemCopyListeners: List<VaultItemCopyListener>
) : VaultItemCopyListenerHolder {
    override fun getVaultItemCopyListener() = vaultItemCopyListeners
}