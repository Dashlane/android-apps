package com.dashlane.network.webservices

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface GetDevicesNumberService {

    @FormUrlEncoded
    @POST(DashlaneUrls.USER_NUMBER_DEVICES)
    suspend fun execute(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): GetDevicesNumberResponse

    class GetDevicesNumberResponse(
        @SerializedName("numberOfDevices")
        val numberOfDevices: Int,
        @SerializedName("status")
        val status: String?
    ) {
        val isSuccess
            get() = status == "success"
    }
}