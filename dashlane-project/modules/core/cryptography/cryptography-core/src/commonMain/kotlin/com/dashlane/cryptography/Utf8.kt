package com.dashlane.cryptography

expect fun CharArray.encodeUtf8ToByteArray(): ByteArray

expect fun CharSequence.encodeUtf8ToByteArray(): ByteArray

expect fun ByteArray.decodeUtf8ToCharArray(): CharArray

expect fun ByteArray.decodeUtf8ToString(): String