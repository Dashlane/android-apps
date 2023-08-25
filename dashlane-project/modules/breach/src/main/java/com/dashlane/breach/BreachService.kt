package com.dashlane.breach

import okhttp3.Call

interface BreachService {

    suspend fun getBreaches(
        login: String,
        uki: String,
        fromRevision: Int = 0,
        revisionOnly: Boolean = false
    ): Result

    companion object Factory {
        operator fun invoke(callFactory: Call.Factory): BreachService =
            BreachServiceImpl(callFactory)
    }

    data class Result(val currentRevision: Int, val breaches: List<BreachWithOriginalJson>?)
}