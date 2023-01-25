package com.dashlane.network.webservices

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface GetSharingLinkService {

    @FormUrlEncoded
    @POST(DashlaneUrls.GET_SHARING_LINK)
    fun createCall(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): Call<Content>

    data class Content(
        @SerializedName("sharingId")
        val sharingId: String
    )
}