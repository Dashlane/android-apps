@file:JvmName("LogFlushFactory")

package com.dashlane.hermes

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dashlane.server.api.Authorization
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val LOGIN_KEY = "login"
private const val ACCESS_TOKEN_KEY = "accessToken"
private const val SIGNATURE_KEY = "signatureKey"

@HiltWorker
internal class HermesWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val logRepository: LogRepository,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            return try {
                val login = inputData.getString(LOGIN_KEY)
                val accessToken = inputData.getString(ACCESS_TOKEN_KEY)
                val signatureKey = inputData.getByteArray(SIGNATURE_KEY)

                val authorization =
                    if (login != null && accessToken != null && signatureKey != null) {
                        Authorization.User(
                            login = login,
                            accessToken = accessToken,
                            signatureKey = signatureKey,
                        )
                    } else {
                        null
                    }
                logRepository.flushLogs(authorization)
                Result.success()
            } catch (e: HermesException) {
                Result.retry()
            }
        }
    }

@JvmName("create")
@JvmOverloads
@Suppress("FunctionName")
fun LogFlush(
    context: Context,
    workName: String = "hermes",
): LogFlush {
    return LogFlushImpl(
        context.applicationContext,
        workName,
    )
}

private class LogFlushImpl(
    private val context: Context,
    private val workName: String,
) : LogFlush {
    override fun invoke(authorization: Authorization.User?) {
        val dataBuilder = Data.Builder()
        authorization?.let {
            dataBuilder.putString(LOGIN_KEY, authorization.login)
            dataBuilder.putString(ACCESS_TOKEN_KEY, authorization.accessToken)
            dataBuilder.putByteArray(SIGNATURE_KEY, authorization.signatureKey)
        }
        val inputData = dataBuilder.build()

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val workRequest =
            OneTimeWorkRequestBuilder<HermesWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, workRequest)
    }
}