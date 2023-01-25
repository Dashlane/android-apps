package com.dashlane.network.webservices.authentication

import com.dashlane.network.BaseNetworkResponse
import com.dashlane.network.webservices.DashlaneUrls
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface GetTokenService {

    @FormUrlEncoded
    @POST(DashlaneUrls.GET_TOKEN)
    suspend fun execute(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): BaseNetworkResponse<Content>

    @FormUrlEncoded
    @POST(DashlaneUrls.GET_TOKEN)
    fun createCall(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): Call<BaseNetworkResponse<Content>>

    data class Content(
        @SerializedName("token")
        val token: String
    )
}