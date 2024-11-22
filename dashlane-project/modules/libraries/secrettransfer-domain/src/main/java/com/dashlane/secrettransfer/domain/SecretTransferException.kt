package com.dashlane.secrettransfer.domain

class SecretTransferException(val error: SecretTransferError) : Exception()