package com.dashlane.network.webservices

import com.dashlane.network.BaseNetworkResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface GetCountryService {

    @GET(DashlaneUrls.COUNTRY)
    suspend fun getCountry(): BaseNetworkResponse<Content>

    class Content(
        @SerializedName("country")
        val country: String?,
        @SerializedName("isEu")
        val isInEuropeanUnion: Boolean?
    )
}