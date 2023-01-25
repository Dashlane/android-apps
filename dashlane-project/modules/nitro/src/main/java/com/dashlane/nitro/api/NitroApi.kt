package com.dashlane.nitro.api

import com.dashlane.nitro.api.endpoints.ConfirmLoginService
import com.dashlane.nitro.api.endpoints.RequestLoginService

interface NitroApi {
    val endpoints: Endpoints

    interface Endpoints {
        val requestLoginService: RequestLoginService
        val confirmLoginService: ConfirmLoginService
    }
}