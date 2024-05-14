package com.dashlane.nitro.api

import com.dashlane.nitro.api.endpoints.ConfirmLogin2Service
import com.dashlane.nitro.api.endpoints.RequestLogin2Service

interface NitroApi {
    val endpoints: Endpoints

    interface Endpoints {
        val requestLogin2Service: RequestLogin2Service
        val confirmLogin2Service: ConfirmLogin2Service
    }
}