package com.dashlane.cryptography

import okio.buffer
import okio.source

fun EncryptedFile.asDecryptionSource(): DecryptionSource =
    DecryptionSource { value.source().buffer() }