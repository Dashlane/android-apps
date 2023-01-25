package com.dashlane.device

import android.os.Build
import com.google.gson.annotations.SerializedName

data class DeviceInformation(
    @SerializedName("osVersion")
    private val osVersion: String = Build.VERSION.RELEASE,
    @SerializedName("manufacturer")
    private val manufacturer: String = Build.MANUFACTURER,
    @SerializedName("model")
    private val model: String = Build.MODEL,
    @SerializedName("crashReportId")
    var crashReportId: String?,
    @SerializedName("dashlaneAppSignature")
    var dashlaneAppSignature: String?,
    @SerializedName("installerOrigin")
    var installerOrigin: String?,
    @SerializedName("mpResetBiometric")
    var hasMPReset: String?,
    @SerializedName("appInstallDate")
    var appInstallDate: String?,
    @SerializedName("autofillEnabled")
    var autofillEnabled: String?,
    @SerializedName("racletteDatabase")
    var racletteDatabase: String?
)
