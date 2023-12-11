package com.dashlane.credentialmanager.model.fido

object AuthenticatorFlags {
    const val USER_PRESENT = 0x01
    const val USER_VERIFIED = 0x04
    const val BACKUP_ELIGIBILITY = 0x08
    const val BACKUP_STATE = 0x10
    const val ATTESTED_CRED_DATA_INCLUDED = 0x40
    const val EXTENSION_DATA_INCLUDED = 0x80
}