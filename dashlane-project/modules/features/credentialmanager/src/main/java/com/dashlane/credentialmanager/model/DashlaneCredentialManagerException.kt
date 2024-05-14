package com.dashlane.credentialmanager.model

open class DashlaneCredentialManagerException(message: String? = null) : Exception(message)
class PasskeyPrivateKeyException : DashlaneCredentialManagerException()
class PasskeyUnsupportedAlgorithmException(message: String) : DashlaneCredentialManagerException(message)
class ItemNotFoundException(message: String) : DashlaneCredentialManagerException(message)
class MissingFieldException(message: String) : DashlaneCredentialManagerException(message)
class MissingArgumentException(message: String) : DashlaneCredentialManagerException(message)
class UnsupportedCredentialTypeException(message: String) : DashlaneCredentialManagerException(message)
class PasswordLimitReachedException(message: String) : DashlaneCredentialManagerException(message)
