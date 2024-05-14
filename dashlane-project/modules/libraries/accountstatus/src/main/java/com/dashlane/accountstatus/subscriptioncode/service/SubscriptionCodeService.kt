package com.dashlane.accountstatus.subscriptioncode.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SubscriptionCodeService {
    @FormUrlEncoded
    @POST("/3/premium/getSubscriptionCode")
    suspend fun getSubscriptionCode(
        @Field("login") login: String,
        @Field("uki") uki: String
    ): SubscriptionCodeResponse

    data class SubscriptionCodeResponse(
        @SerializedName("code")
        val code: Int,

        @SerializedName("message")
        val message: String?,

        @SerializedName("content")
        val content: Content,
    ) {
        data class Content(
            @SerializedName("subscriptionCode")
            val subscriptionCode: String
        )
    }
}