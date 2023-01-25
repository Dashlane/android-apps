package com.dashlane.network.webservices

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part



interface CrashReportUploadService {

    @Multipart
    @POST("/1/crashreports/upload")
    suspend fun upload(@Part file: MultipartBody.Part): Response

    data class Response(
        @SerializedName("result")
        val result: String?
    ) {
        val isSuccess
            get() = result == "Success"
    }
}