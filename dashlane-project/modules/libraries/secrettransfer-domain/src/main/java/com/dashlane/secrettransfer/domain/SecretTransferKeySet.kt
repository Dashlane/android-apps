package com.dashlane.secrettransfer.domain

data class SecretTransferKeySet(
    val publicKey: SecretTransferPublicKey,
    val privateKey: String,
    val transferId: String,
    val secretTransferUri: SecretTransferUri
)