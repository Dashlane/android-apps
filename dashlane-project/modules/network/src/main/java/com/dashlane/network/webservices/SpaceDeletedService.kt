package com.dashlane.network.webservices

import com.dashlane.network.BaseNetworkResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SpaceDeletedService {

    @FormUrlEncoded
    @POST(DashlaneUrls.SPACE_DELETED)
    fun createCall(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("teamId") teamId: String
    ): Call<BaseNetworkResponse<String>>
}