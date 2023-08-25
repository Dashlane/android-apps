package com.dashlane.network.webservices.fcm

import com.dashlane.network.webservices.DashlaneUrls
import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PushNotificationService {

    @FormUrlEncoded
    @POST(DashlaneUrls.PUSH_NOTIFICATION)
    suspend fun setPushNotificationId(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("type") type: String = "google",
        @Field("pushID") pushID: String,
        @Field("sendToAppboy") sendToAppboy: String = true.toString()
    ): Response

    class Response(
        @SerializedName("code")
        var code: Int?,
        @SerializedName("message")
        var message: String?
    ) {
        val isSuccess: Boolean
            get() = message == "OK"
    }
}
