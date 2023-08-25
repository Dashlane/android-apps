package com.dashlane.plans

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SubscriptionCodeService {
    @FormUrlEncoded
    @POST("/3/premium/getSubscriptionCode")
    fun getSubscriptionCode(@Field("login") login: String, @Field("uki") uki: String): Call<SubscriptionCodeResponse>

    class SubscriptionCodeResponse {
        @SerializedName("code")
        val code: Int = 0

        @SerializedName("message")
        val message: String? = null

        @SerializedName("content")
        val content: JsonObject? = null

        val subscriptionCode: String? by lazy { content?.get("subscriptionCode")?.asString }
    }
}