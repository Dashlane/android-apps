package com.dashlane.autofill.util

import com.dashlane.util.uuid.UuidFactory
import javax.inject.Inject

class IdGeneratorImpl @Inject constructor(
    private val uuidFactory: UuidFactory,
) : IdGenerator {
    override fun nextId(): String = uuidFactory.generateUuid()
}