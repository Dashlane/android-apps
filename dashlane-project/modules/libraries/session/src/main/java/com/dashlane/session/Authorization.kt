package com.dashlane.session

import com.dashlane.server.api.Authorization

val Session.authorization
    get() = Authorization.User(userId, accessKey, secretKey)