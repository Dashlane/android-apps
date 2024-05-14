package com.dashlane.nitro

import com.dashlane.nitro.cryptography.SecretStreamServerInfo

internal interface AttestationValidator {
    fun validate(attestation: CoseSign1): SecretStreamServerInfo
}