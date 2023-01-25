package com.dashlane.securefile.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.IOException



interface GetUploadLinkService {

    @POST("/1/securefile/getUploadLink")
    @FormUrlEncoded
    suspend fun createCall(
        @Field("login")
        login: String,
        @Field("uki")
        uki: String,
        @Field("contentLength")
        contentLengthBytes: Long,
        @Field("secureFileInfoId")
        secureFileInfoId: String?
    ): Response

    class Response(
        @SerializedName("code")
        val code: Int,
        @SerializedName("message")
        val message: String,
        @SerializedName("content")
        @JsonAdapter(Content.TypeAdapterFactory::class)
        val content: Content?
    ) {
        

        class Content(
            @SerializedName("url")
            val url: String,
            @SerializedName("fields")
            val fields: JsonObject,
            @SerializedName("key")
            val fileId: String,
            @SerializedName("quota")
            val quota: Quota,
            @SerializedName("acl")
            val acl: String,
            val errorMessage: String?
        ) {

            

            class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): com.google.gson.TypeAdapter<T> {
                    if (type.rawType != Content::class.java) throw IllegalArgumentException("Unexpected type $type")
                    return TypeAdapter(gson) as com.google.gson.TypeAdapter<T>
                }
            }

            class TypeAdapter(val gson: Gson) : com.google.gson.TypeAdapter<GetUploadLinkService.Response.Content>() {
                override fun read(reader: JsonReader): GetUploadLinkService.Response.Content {
                    val token = reader.peek()
                    return when (token) {
                        JsonToken.BEGIN_OBJECT -> gson.fromJson(
                            reader,
                            GetUploadLinkService.Response.Content::class.java
                        )
                        JsonToken.STRING -> {
                            val errorMessage = reader.nextString()
                            return GetUploadLinkService.Response.Content(
                                "", JsonObject(), "",
                                Quota(0, 0), "", errorMessage
                            )
                        }
                        else -> throw IOException("Unexpected token $token")
                    }
                }

                override fun write(writer: JsonWriter, value: GetUploadLinkService.Response.Content?) {
                    throw UnsupportedOperationException()
                }
            }

            

            class Quota(
                @SerializedName("remaining")
                val remainingBytes: Long,
                @SerializedName("max")
                val maxBytes: Long
            )
        }
    }
}