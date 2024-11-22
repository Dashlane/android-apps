package com.dashlane.securefile

import com.google.gson.annotations.SerializedName

class Attachment {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("filename")
    var filename: String? = null

    @SerializedName("downloadKey")
    var downloadKey: String? = null

    @SerializedName("cryptoKey")
    var cryptoKey: String? = null

    @SerializedName("localSize")
    var localSize: Long? = null

    @SerializedName("remoteSize")
    var remoteSize: Long? = null

    @SerializedName("creationDatetime")
    var creationDatetime: Long? = null

    @SerializedName("userModificationDatetime")
    var userModificationDatetime: Long? = null

    @SerializedName("owner")
    var owner: String? = null

    @SerializedName("version")
    var version: Int? = null

    fun isSupportedVersion(): Boolean {
        version ?: return false
        return version!! <= MAX_SUPPORTED_VERSION
    }

    companion object {
        private const val MAX_SUPPORTED_VERSION = 1
    }
}