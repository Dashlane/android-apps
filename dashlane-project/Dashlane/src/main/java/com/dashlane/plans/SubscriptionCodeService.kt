package com.dashlane.plans

import com.google.gson.JsonObject
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

    class SubscriptionCodeResponse {
        @SerializedName("code")
        val code: Int = 0

        @SerializedName("message")
        val message: String? = null

        @SerializedName("content")
        val content: JsonObject? = null
    }
}