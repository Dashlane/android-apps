package com.dashlane.data.login

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: String,
    val name: String,
    val iconResId: Int,
    val lastActivityDate: Long,
    val pairingGroupId: String? = null,
    var selected: Boolean = false,
) : Parcelable