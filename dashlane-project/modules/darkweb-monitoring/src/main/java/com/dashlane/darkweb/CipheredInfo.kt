package com.dashlane.darkweb

import com.google.gson.annotations.SerializedName



internal data class CipheredInfo(
    @SerializedName("breachId")
    val breachId: String? = null,
    @SerializedName("data")
    val data: List<Data>? = null
) {
    data class Data(
        @SerializedName("type")
        val type: String? = null,
        @SerializedName("hashMethod")
        val hashMethod: String? = null,
        @SerializedName("value")
        val value: String? = null

    )
}