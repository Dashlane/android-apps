package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.Gender?.getLabelId() = when (this) {
    SyncObject.Gender.MALE -> R.string.male
    SyncObject.Gender.FEMALE -> R.string.female
    null -> R.string.no_gender
}
