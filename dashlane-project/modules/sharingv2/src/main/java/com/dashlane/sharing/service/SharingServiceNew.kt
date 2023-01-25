package com.dashlane.sharing.service

import com.dashlane.sharing.service.response.FindUsersResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface SharingServiceNew {
    @FormUrlEncoded
    @POST("/1/userAlias/findUsers")
    suspend fun findUsers(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("aliases") aliases: ObjectToJson<List<String>>
    ): FindUsersResponse
}