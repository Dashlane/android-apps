package com.dashlane.util.clipboard

import android.content.Context
import com.dashlane.util.clipboard.vault.VaultItemClipboard



interface CopyComponent {
    val clipboardCopy: ClipboardCopy
    val vaultItemClipboard: VaultItemClipboard

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as CopyApplication).component
    }
}