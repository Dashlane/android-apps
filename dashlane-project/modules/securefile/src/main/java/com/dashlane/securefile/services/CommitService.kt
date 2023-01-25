package com.dashlane.securefile.services

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface CommitService {

    @POST("/1/securefile/commit")
    @FormUrlEncoded
    suspend fun commit(
        @Field("login")
        login: String,
        @Field("uki")
        uki: String,
        @Field("key")
        fileId: String,
        @Field("secureFileInfoId")
        secureFileInfoId: String
    ): Response

    data class Response(
        @SerializedName("code")
        val code: Int,
        @SerializedName("message")
        val message: String,
        @SerializedName("content")
        val content: Content?
    ) {
        

        data class Content(
            @SerializedName("success")
            val success: Boolean,
            @SerializedName("quota")
            val quota: Quota
        ) {

            

            data class Quota(
                @SerializedName("remaining")
                val remainingBytes: Long,
                @SerializedName("max")
                val maxBytes: Long
            )
        }
    }
}
