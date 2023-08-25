package com.dashlane.breach

import com.dashlane.network.BaseNetworkResponse
import com.dashlane.network.webservices.DashlaneUrls
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

class BreachServiceImpl constructor(
    private val gson: Gson,
    private val breachQueryService: BreachQueryService,
    private val breachEntriesService: BreachEntriesService
) : BreachService {

    override suspend fun getBreaches(
        login: String,
        uki: String,
        fromRevision: Int,
        revisionOnly: Boolean
    ) = withContext(Dispatchers.Default) {
        val queryResponse = breachQueryService.call(login, uki, fromRevision)
        check(queryResponse.code == 200) { "Breaches query response $queryResponse" }
        val breachWsResult = queryResponse.content ?: error("breach result == null $queryResponse")

        val breaches = if (!revisionOnly) {
            val breachesFromWs = breachWsResult.breaches?.map { toBreach(it) }
            val breachesFromFiles = breachWsResult.fileUrls?.map { breachEntriesService.call(it) }
                ?.flatMap {
                    val itemsResponse = it
                    itemsResponse.breaches ?: error("Breaches == null $itemsResponse")
                }
                ?.map { toBreach(it) }
            mergeBreaches(breachesFromFiles, breachesFromWs)
        } else {
            null
        }
        BreachService.Result(breachWsResult.revision ?: 0, breaches)
    }

    private fun toBreach(it: JsonObject): BreachWithOriginalJson {
        val originalJson = it.toString()
        val breach = gson.fromJson(it, Breach::class.java)
        return BreachWithOriginalJson(breach, originalJson)
    }

    private fun mergeBreaches(vararg lists: List<BreachWithOriginalJson>?): List<BreachWithOriginalJson> {
        return lists.filterNotNull()
            .flatten()
            .groupBy { it.breach.id }
            .values
            .mapNotNull { it.maxByOrNull { it.breach.lastModificationRevision } }
    }

    companion object Factory {
        operator fun invoke(callFactory: Call.Factory): BreachServiceImpl {
            val retrofit = Retrofit.Builder()
                .callFactory(callFactory)
                .baseUrl(DashlaneUrls.URL_WEBSERVICES)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val queryService = retrofit.create(BreachQueryService::class.java)
            val entriesService = retrofit.create(BreachEntriesService::class.java)
            return BreachServiceImpl(Gson(), queryService, entriesService)
        }
    }

    interface BreachQueryService {

        @POST("/1/breaches/get")
        @FormUrlEncoded
        suspend fun call(
            @Field("login") login: String,
            @Field("uki") uki: String,
            @Field("revision") revision: Int
        ): BaseNetworkResponse<Content>

        data class Content(
            @SerializedName("revision")
            val revision: Int?,
            @SerializedName("filesToDownload")
            val fileUrls: List<String>?,
            @SerializedName("latest")
            val breaches: List<JsonObject>?
        )
    }

    interface BreachEntriesService {
        @GET
        suspend fun call(@Url url: String): Response

        data class Response(
            @SerializedName("breaches")
            val breaches: List<JsonObject>?
        )
    }
}