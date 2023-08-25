package com.dashlane.network.webservices.authentication

import com.dashlane.network.BaseNetworkResponse
import com.dashlane.network.webservices.DashlaneUrls
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OtpPhoneLostService {

    @FormUrlEncoded
    @POST(DashlaneUrls.OTP_PHONE_LOST)
    suspend fun execute(
        @Field("login") login: String
    ): BaseNetworkResponse<Unit>
}
