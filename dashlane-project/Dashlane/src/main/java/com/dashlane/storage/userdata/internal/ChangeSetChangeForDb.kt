package com.dashlane.storage.userdata.internal

data class ChangeSetChangeForDb(
    val sqliteId: Long = 0,
    val uid: String,
    val changeSetUID: String,
    val changedProperty: String,
    val currentValue: String? = null,
    val isSavedFromJava: Boolean = false
)