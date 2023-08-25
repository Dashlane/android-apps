package com.dashlane.network.webservices

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Url

interface UploadFileService {

    @Multipart
    @POST
    suspend fun execute(
        @Url
        url: String,
        @PartMap
        body: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part
        encryptedFile: MultipartBody.Part
    ): Response<Unit> 
}