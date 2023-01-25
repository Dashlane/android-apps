package com.dashlane.network.webservices

import com.dashlane.network.BaseNetworkResponse
import com.google.gson.JsonObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface FeatureFlipService {

    @FormUrlEncoded
    @POST(DashlaneUrls.FEATURE_FLIPPING)
    suspend fun execute(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("features") features: String
    ): BaseNetworkResponse<JsonObject>
}
