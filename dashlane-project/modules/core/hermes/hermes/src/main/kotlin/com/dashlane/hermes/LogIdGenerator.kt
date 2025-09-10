package com.dashlane.hermes

import java.util.UUID

interface LogIdGenerator {
    fun generateUuid(): UUID
}

class LogIdGeneratorImpl : LogIdGenerator {
    override fun generateUuid(): UUID = UUID.randomUUID()
}