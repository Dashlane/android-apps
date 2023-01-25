package com.dashlane.sync.cryptochanger

import java.util.UUID
import javax.inject.Inject

interface RemoteKeyIdGenerator {
    fun generateRemoteKeyId(): String
}

class RemoteKeyIdGeneratorImpl @Inject constructor() : RemoteKeyIdGenerator {
    override fun generateRemoteKeyId(): String =
        UUID.randomUUID().toString()
}
