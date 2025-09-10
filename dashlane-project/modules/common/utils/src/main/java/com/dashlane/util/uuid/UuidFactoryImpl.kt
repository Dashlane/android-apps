package com.dashlane.util.uuid

import java.util.UUID
import javax.inject.Inject

class UuidFactoryImpl @Inject constructor() : UuidFactory {
    override fun generateUuid() = UUID.randomUUID().toString()
}