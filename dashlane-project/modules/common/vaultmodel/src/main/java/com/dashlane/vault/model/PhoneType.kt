package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.Phone.Type.getStringId(): Int {
    return when (this) {
        SyncObject.Phone.Type.PHONE_TYPE_MOBILE -> R.string.phone_type_mobile
        SyncObject.Phone.Type.PHONE_TYPE_WORK_LANDLINE -> R.string.phone_type_work_landline
        SyncObject.Phone.Type.PHONE_TYPE_WORK_MOBILE -> R.string.phone_type_work_mobile
        SyncObject.Phone.Type.PHONE_TYPE_WORK_FAX -> R.string.phone_type_work_fax
        SyncObject.Phone.Type.PHONE_TYPE_FAX -> R.string.phone_type_fax
        SyncObject.Phone.Type.PHONE_TYPE_LANDLINE -> R.string.phone_type_landline
        SyncObject.Phone.Type.PHONE_TYPE_ANY -> R.string.phone_type_any
    }
}