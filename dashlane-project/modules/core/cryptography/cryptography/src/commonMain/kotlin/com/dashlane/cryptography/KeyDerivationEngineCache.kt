package com.dashlane.cryptography

import okio.Closeable

internal expect class KeyDerivationEngineCache : KeyDerivationEngine, Closeable

internal expect fun KeyDerivationEngine.cache(): KeyDerivationEngineCache