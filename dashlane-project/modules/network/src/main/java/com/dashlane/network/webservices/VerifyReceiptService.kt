package com.dashlane.network.webservices

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface VerifyReceiptService {
    companion object {
        private const val PLAY_STORE_ORIGIN_HTTP = "playstore"
    }

    @FormUrlEncoded
    @POST(DashlaneUrls.VERIFY_RECEIPT)
    suspend fun verifyReceipt(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("receipt") receipt: String,
        @Field("origin") origin: String = PLAY_STORE_ORIGIN_HTTP,
        @Field("plan") plan: String?,
        @Field("currency") currency: String? = null,
        @Field("amount") amount: String? = null
    ): VerifyReceiptResponse

    

    data class VerifyReceiptResponse(
        @SerializedName("success")
        val success: Boolean,
        @SerializedName("planType")
        val planType: String
    ) {
        companion object {
            private const val PLAYSTORE_RENEWABLE = "playstore_renewable"
        }

        val isRenewable: Boolean
            get() = PLAYSTORE_RENEWABLE == planType
    }
}