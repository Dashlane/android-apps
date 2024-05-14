package com.dashlane.authentication

import java.util.UUID
import javax.inject.Inject

class UuidFactoryImpl @Inject constructor() : UuidFactory {
    override fun generateUuid() = UUID.randomUUID().toString()
}