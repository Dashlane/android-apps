package com.dashlane.network.tools

import com.dashlane.server.api.Authorization
import com.dashlane.session.Session

val Session.authorization
    get() = Authorization.User(userId, accessKey, secretKey)