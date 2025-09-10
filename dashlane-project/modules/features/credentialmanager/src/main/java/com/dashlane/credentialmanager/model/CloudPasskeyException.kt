package com.dashlane.credentialmanager.model

import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException

open class CloudPasskeyException(message: String) : DashlaneCredentialManagerException(message)

class CloudPasskeyNetworkException(message: String) : CloudPasskeyException(message = message)

class CloudPasskeyInvalidPublicKeyException(message: String) : CloudPasskeyException(message = message)