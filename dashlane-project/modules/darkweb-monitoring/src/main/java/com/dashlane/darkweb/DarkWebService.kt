package com.dashlane.darkweb

import com.dashlane.network.BaseNetworkResponse
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface DarkWebService {

    @POST("/1/dataleak/optin")
    @FormUrlEncoded
    suspend fun optIn(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("emails") emails: JSONArray
    ): BaseNetworkResponse<OptInContent>

    @POST("/1/dataleak/optout")
    @FormUrlEncoded
    suspend fun optOut(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("emails") emails: JSONArray
    ): BaseNetworkResponse<Unit>

    @POST("/1/dataleak/status")
    @FormUrlEncoded
    suspend fun getEmailStatus(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("wantsLeaks") wantsLeaks: Boolean = false
    ): BaseNetworkResponse<EmailStatusContent>

    @POST("/1/dataleak/leaks")
    @FormUrlEncoded
    suspend fun getBreaches(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("includeDisabled") includeDisabled: Boolean,
        @Field("wantsDetails") wantsDetails: Boolean,
        @Field("lastUpdateDate") lastUpdateDate: Long?
    ): BaseNetworkResponse<BreachStatusContent>

    class BreachStatusContent {
        @SerializedName("leaks")
        var breaches: List<JsonObject>? = null
        @SerializedName("details")
        var details: Detail? = null
        @SerializedName("lastUpdateDate")
        var lastUpdateDate: Long = 0

        data class Detail(
            @SerializedName("cipheredKey")
            var cipheredKey: String? = null,
            @SerializedName("cipheredInfo")
            var cipheredInfo: String? = null
        )
    }

    class EmailStatusContent {
        @SerializedName("emails")
        var emailsState: List<EmailWithState>? = null

        data class EmailWithState(
            @SerializedName("email") val email: String?,
            @SerializedName("state") val state: String?
        )
    }

    class OptInContent {

        @SerializedName("results")
        var emailsWithResult: List<EmailWithResult>? = null

        data class EmailWithResult(
            @SerializedName("email") val email: String?,
            @SerializedName("result") val status: String?
        )
    }
}