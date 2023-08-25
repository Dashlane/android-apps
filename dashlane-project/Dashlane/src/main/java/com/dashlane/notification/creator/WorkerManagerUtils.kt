package com.dashlane.notification.creator

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

suspend inline fun <reified W : ListenableWorker> WorkManager.updateIfExist(tag: String, duration: Long) {
    val (enqueued, succeeded) = findEnqueuedAndSucceeded(tag)

    
    enqueued?.run {
        cancelAllWorkByTag(tag)
    }

    
    if (succeeded == null) {
        createWorker<W>(tag, duration)
    }
}

suspend inline fun <reified W : ListenableWorker> WorkManager.createIfNonExist(tag: String, duration: Long) {
    val (enqueued, succeeded) = findEnqueuedAndSucceeded(tag)

    
    if (enqueued != null) return

    
    if (succeeded == null) {
        createWorker<W>(tag, duration)
    }
}

suspend inline fun <reified W : ListenableWorker> WorkManager.createPeriodicIfNonExist(
    tag: String,
    duration: Long,
    initialDelay: Long = duration
) {
    val (enqueued, succeeded) = findEnqueuedAndSucceeded(tag)

    
    if (enqueued != null) return

    
    if (succeeded == null) {
        createPeriodicWorker<W>(tag, duration, initialDelay)
    }
}

inline fun <reified W : ListenableWorker> WorkManager.createWorker(tag: String, duration: Long) {
    val work = OneTimeWorkRequestBuilder<W>()
        .setInitialDelay(duration, TimeUnit.MILLISECONDS)
        .addTag(tag)
        .build()
    enqueue(work)
}

inline fun <reified W : ListenableWorker> WorkManager.createPeriodicWorker(
    tag: String,
    periodicDelay: Long,
    initialDelay: Long
) {
    val work = PeriodicWorkRequestBuilder<W>(periodicDelay, TimeUnit.MILLISECONDS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .addTag(tag)
        .build()
    enqueue(work)
}

suspend fun <T> ListenableFuture<T>.await(): T = withContext(Dispatchers.Default) { get() }

suspend fun WorkManager.findEnqueuedAndSucceeded(tag: String): Pair<WorkInfo?, WorkInfo?> {
    val list = getWorkInfosByTag(tag).await()
    return list.find {
        it.state == WorkInfo.State.ENQUEUED
    } to list.find {
        it.state == WorkInfo.State.SUCCEEDED
    }
}

fun getWorkManagerDuration(
    durationByDay: Int,
    hourOfDay: Int,
    clock: Clock = Clock.systemDefaultZone()
): Long {
    val time = ZonedDateTime.now(clock)
        .plus(durationByDay.toLong(), ChronoUnit.DAYS)
        .withHour(hourOfDay)
    return Duration.between(Instant.now(clock), time.toInstant()).toMillis()
}
