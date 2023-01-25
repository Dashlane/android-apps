package com.dashlane.util.clipboard.vault



interface VaultItemCopyListenerHolder {
    fun getVaultItemCopyListener(): List<VaultItemCopyListener>
}