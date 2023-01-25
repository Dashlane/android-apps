package com.dashlane.device

import com.dashlane.sharing.service.ObjectToJson
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface DeviceService {
    @FormUrlEncoded
    @POST("/1/devices/updateDeviceInformation")
    fun updateDeviceInformation(
        @Field("login") login: String,
        @Field("uki") uki: String,
        @Field("deviceInformation") deviceInformation: ObjectToJson<DeviceInformation>
    ): Call<String>
}
