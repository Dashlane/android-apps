package com.dashlane.device

import com.dashlane.util.TextUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class DeviceUpdateCallback(private val listener: Listener) : Callback<String> {

    override fun onFailure(call: Call<String>?, t: Throwable?) {
        listener.onFail()
    }

    override fun onResponse(call: Call<String>?, response: Response<String>?) {
        val responseBody = response?.body()
        if (responseBody != null && TextUtil.isServerResponsePositive(responseBody)) {
            listener.onSuccess()
        } else {
            listener.onFail()
        }
    }

    interface Listener {
        fun onSuccess()
        fun onFail()
    }
}
