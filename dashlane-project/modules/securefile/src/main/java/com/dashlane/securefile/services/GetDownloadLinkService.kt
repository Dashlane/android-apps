package com.dashlane.securefile.services

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface GetDownloadLinkService {

    @POST("/1/securefile/getDownloadLink")
    @FormUrlEncoded
    suspend fun execute(
        @Field("login")
        login: String,
        @Field("uki")
        uki: String,
        @Field("key")
        fileId: String
    ): Response

    class Response(
        @SerializedName("code")
        val code: Int,
        @SerializedName("message")
        val message: String?,
        @SerializedName("content")
        val content: Content?
    ) {
        

        class Content(
            @SerializedName("url")
            val url: String
        )
    }
}