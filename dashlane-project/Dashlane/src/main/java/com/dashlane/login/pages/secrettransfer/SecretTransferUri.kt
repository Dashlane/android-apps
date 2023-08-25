package com.dashlane.login.pages.secrettransfer

import android.net.Uri
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder

private const val TRANSFER_ID_KEY = "id"
private const val PUBLIC_KEY_KEY = "key"

data class SecretTransferUri(
    val transferId: String,
    val publicKey: String
) {

    companion object {
        fun fromUri(uri: Uri): SecretTransferUri? {
            if (uri.scheme != NavigationHelper.Destination.SCHEME || uri.pathSegments[0] != NavigationHelper.Destination.MainPath.SECRET_TRANSFER) {
                return null
            }
            val transferId = uri.getQueryParameter(TRANSFER_ID_KEY) ?: return null
            val publicKey = uri.getQueryParameter(PUBLIC_KEY_KEY) ?: return null
            return SecretTransferUri(transferId = transferId, publicKey = publicKey)
        }
    }

    val uri: Uri = NavigationUriBuilder()
        .host(NavigationHelper.Destination.MainPath.SECRET_TRANSFER)
        .appendQueryParameter(TRANSFER_ID_KEY, transferId)
        .appendQueryParameter(PUBLIC_KEY_KEY, publicKey)
        .build()
}
