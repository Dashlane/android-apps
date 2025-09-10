package com.dashlane.devicediscovery

import com.google.android.gms.wearable.Node

data class PairedDevice(
    val id: String,
    val name: String,
    val isNearby: Boolean,
    val hasDashlane: Boolean,
)