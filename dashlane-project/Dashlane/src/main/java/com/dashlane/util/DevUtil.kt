package com.dashlane.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dashlane.R
import com.dashlane.core.helpers.DashlaneHelper
import com.dashlane.logger.ExceptionLog
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

object DevUtil {

    

    @JvmOverloads
    @JvmStatic
    fun startActivityOrDefaultErrorMessage(context: Context, intent: Intent, addFlagActivity: Boolean = true) {
        if (intent.resolveActivity(context.packageManager) == null) {
            ToasterImpl(context).show(
                context.getString(R.string.contact_system_administrator),
                Toast.LENGTH_LONG
            )
        } else {
            if (context is Activity) {
                if (addFlagActivity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    object DevicePerformance {
        @JvmStatic
        val numberOfCpuCores: Int
            get() = Runtime.getRuntime().availableProcessors()

        
        
        @JvmStatic
        val totalAmountOfRam: Long
            get() {
                try {
                    FileReader("/proc/meminfo").use { localFileReader ->
                        BufferedReader(localFileReader, 8192).use { localBufferedReader ->
                            val str2 = localBufferedReader.readLine() 
                            val arrayOfString = str2.split(Regex("\\s+")).toTypedArray()
                            
                            return arrayOfString[1].toInt() / 1024L
                        }
                    }
                } catch (e: IOException) {
                    return -1
                }
            }

        @JvmStatic
        val ramUsage: Float
            get() {
                try {
                    val runtime = Runtime.getRuntime()
                    return ((runtime.totalMemory() - runtime.freeMemory()) / 1048576L).toFloat()
                } catch (ex: Exception) {
                    ExceptionLog.v(ex)
                }
                return 0F
            }

        @JvmStatic
        val heapUsage: Float
            get() {
                try {
                    val runtime = Runtime.getRuntime()
                    return (runtime.maxMemory() / 1048576L).toFloat()
                } catch (ex: Exception) {
                    ExceptionLog.v(ex)
                }
                return 0F
            }

        @JvmStatic
        val timeSinceLaunch: Long
            get() = DashlaneHelper.getTimeSinceLaunch()
    }
}