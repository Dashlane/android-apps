package com.dashlane.securefile.services

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface DeleteService {

    @POST("/1/securefile/delete")
    @FormUrlEncoded
    suspend fun delete(
        @Field("login")
        login: String,
        @Field("uki")
        uki: String,
        @Field("secureFileInfoId")
        secureFileInfoId: String
    )
}
