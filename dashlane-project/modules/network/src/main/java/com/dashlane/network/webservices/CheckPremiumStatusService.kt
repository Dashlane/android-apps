package com.dashlane.network.webservices

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface CheckPremiumStatusService {

    @SuppressWarnings("kotlin:S107")
    @FormUrlEncoded
    @POST(DashlaneUrls.PREMIUM_STATUS)
    suspend fun execute(
        @Field("login")
        username: String,
        @Field("uki")
        uki: String,
        @Field("language")
        language: String,
        @Field("platform")
        platform: String,
        @Field("currentTimestamp")
        includeCurrentTimestamp: String = true.toString(),
        @Field("previousPlan")
        includePreviousPlan: String = true.toString(),
        @Field("autoRenewal")
        autoRenewal: String = true.toString(),
        @Field("spaces")
        spaces: String = true.toString(),
        @Field("capabilities")
        capabilities: String = true.toString(),
        @Field("familyInformation")
        includeFamilyInformation: String = true.toString(),
        @Field("playStoreSubscriptionInfo")
        playStoreSubscriptionInfo: String = true.toString(),
        @Field("autoRenewInformation")
        autoRenewInformation: String = true.toString(),
        @Field("checkAdvanced")
        checkAdvanced: String = true.toString()
    ): String
}