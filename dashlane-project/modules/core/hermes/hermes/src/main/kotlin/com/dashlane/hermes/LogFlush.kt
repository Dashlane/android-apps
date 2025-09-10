package com.dashlane.hermes

import com.dashlane.server.api.Authorization

interface LogFlush {
    operator fun invoke(authorization: Authorization.User?)
}