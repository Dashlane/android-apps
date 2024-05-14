package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.Email.Type?.getStringId(): Int {
    return when (this) {
        SyncObject.Email.Type.PRO -> R.string.email_type_work
        SyncObject.Email.Type.PERSO -> R.string.email_type_home
        SyncObject.Email.Type.NO_TYPE, null -> R.string.email_type_home
    }
}