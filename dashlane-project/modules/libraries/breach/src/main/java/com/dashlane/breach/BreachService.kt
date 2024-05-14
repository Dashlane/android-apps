package com.dashlane.breach

interface BreachService {

    suspend fun getBreaches(
        fromRevision: Int = 0,
        revisionOnly: Boolean = false
    ): Result

    data class Result(val currentRevision: Int, val breaches: List<BreachWithOriginalJson>?)
}