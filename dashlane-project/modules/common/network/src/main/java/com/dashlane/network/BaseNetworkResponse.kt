package com.dashlane.network

import com.google.gson.annotations.SerializedName

open class BaseNetworkResponse<T> {
    @SerializedName("code")
    var code: Int = 0

    @SerializedName("message")
    var message: String? = null

    @SerializedName("content")
    var content: T? = null

    fun checkSuccessContent() = if (isSuccess()) content else throw HttpException(code, message)

    fun isSuccess(): Boolean {
        return SUCCESS_CODE == code
    }

    companion object {
        private const val SUCCESS_CODE = 200

        fun <T> createSuccess(content: T) = BaseNetworkResponse<T>().apply {
            this.code = SUCCESS_CODE
            this.content = content
        }
    }

    data class HttpException(val code: Int, val httpMessage: String?) : Exception()
}