package com.dashlane.sharing

import android.content.Context



interface SharingKeysHelperComponent {
    val sharingKeysHelper: SharingKeysHelper

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as SharingKeysHelperApplication).component
    }
}