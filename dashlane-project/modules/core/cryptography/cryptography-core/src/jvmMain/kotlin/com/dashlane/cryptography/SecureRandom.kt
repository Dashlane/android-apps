package com.dashlane.cryptography

import java.security.SecureRandom
import kotlin.random.asKotlinRandom

actual val secureRandom = SecureRandom().asKotlinRandom()