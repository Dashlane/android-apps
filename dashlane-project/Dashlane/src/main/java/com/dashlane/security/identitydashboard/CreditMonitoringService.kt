package com.dashlane.security.identitydashboard

import com.dashlane.network.BaseNetworkResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface CreditMonitoringService {
    @POST("/1/creditmonitoring/getConnectionInfo")
    @FormUrlEncoded
    suspend fun getTransunionLink(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): BaseNetworkResponse<TransunionLinkContent>

    data class TransunionLinkContent(
        @SerializedName("url")
        val url: String?,
        @SerializedName("enrolled")
        val enrolled: String?
    )
}