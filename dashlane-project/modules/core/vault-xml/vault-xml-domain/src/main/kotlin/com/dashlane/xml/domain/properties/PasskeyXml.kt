package com.dashlane.xml.domain.properties

import javax.annotation.processing.Generated

@Generated
object PasskeyXml {
    const val COUNTER = "Counter"
    const val PRIVATE_KEY = "PrivateKey"
    const val CREDENTIAL_ID = "CredentialId"
    const val RP_NAME = "RpName"
    const val USER_HANDLE = "UserHandle"
    const val NOTE = "Note"
    const val CLOUD_PASSKEY = "CloudPasskey"
    const val ITEM_NAME = "ItemName"
    const val USER_DISPLAY_NAME = "UserDisplayName"
    const val RP_ID = "RpId"
    const val KEY_ALGORITHM = "KeyAlgorithm"

    @Generated
    object PrivateKeyXml {
        const val CRV = "crv"
        const val D = "d"
        const val EXT = "ext"
        const val KEY_OPS = "key_ops"
        const val KTY = "kty"
        const val X = "x"
        const val Y = "y"
    }

    @Generated
    object CloudPasskeyXml {
        const val PASSKEY_ENCRYPTION_KEY = "PasskeyEncryptionKey"
        const val PASSKEY_ENCRYPTION_KEY_ID = "PasskeyEncryptionKeyId"
        const val PASSKEY_ID = "PasskeyId"
    }
}