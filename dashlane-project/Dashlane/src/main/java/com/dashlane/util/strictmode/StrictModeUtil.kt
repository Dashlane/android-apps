package com.dashlane.util.strictmode

import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.os.strictmode.DiskReadViolation
import android.os.strictmode.DiskWriteViolation
import android.os.strictmode.UntaggedSocketViolation
import android.os.strictmode.Violation
import androidx.annotation.RequiresApi
import com.dashlane.BuildConfig
import java.util.concurrent.Executors

object StrictModeUtil {
    private const val LEVEL_NONE = "NONE"
    private const val LEVEL_LOG = "LOG"
    private const val LEVEL_CRASH = "CRASH"

    @JvmStatic
    fun init() {
        if (BuildConfig.DEBUG) {
            setRules()
        }
    }

    private val DISK_VIOLATION_WHITELIST = listOf(
        "SharedPreferencesImpl",
        "GlobalPreferencesManager",
        "DeviceInfoRepositoryImpl"
    )

    private fun setRules() {
        val level = BuildConfig.STRICT_MODE_LEVEL
        if (level == LEVEL_NONE) return

        val executor = Executors.newSingleThreadExecutor()
        
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().apply {
                detectAll()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    penaltyListener(
                        executor,
                        StrictMode.OnThreadViolationListener { violation ->
                        when (violation) {
                            is DiskWriteViolation,
                            is DiskReadViolation -> {
                                if (isViolationException(violation, DISK_VIOLATION_WHITELIST)) {
                                    
                                    return@OnThreadViolationListener
                                }
                            }
                        }
                        handleViolation(level, violation)
                    }
                    )
                }
            }.build()
        )

        
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .apply {
                    detectAll()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        penaltyListener(
                            executor,
                            StrictMode.OnVmViolationListener { violation ->
                            when (violation) {
                                is UntaggedSocketViolation -> {
                                    
                                    return@OnVmViolationListener
                                }
                            }
                            handleViolation(level, violation)
                        }
                        )
                    }
                }.build()
        )
    }

    private fun isViolationException(
        violation: Violation,
        exceptions: List<String>
    ): Boolean {
        exceptions.forEach { exception ->
            if (violation.stackTrace.any { it.toString().contains(exception) }) {
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleViolation(level: String, violation: Violation) {
        val header = "StrictMode policy violation; ${violation::class.java.simpleName}:"
        when (level) {
            LEVEL_CRASH -> throw RuntimeException(header, violation)
        }
    }
}
