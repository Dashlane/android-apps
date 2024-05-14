package com.dashlane.breach

import com.dashlane.server.api.endpoints.breaches.GetBreachesService
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Url

interface GetFileBreachesService {

    @GET
    suspend fun execute(
        @Url url: String
    ): Response

    data class Response(
        @SerializedName("breaches")
        val breaches: List<GetBreachesService.Data.LatestBreache>,
    )
}