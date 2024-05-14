package com.dashlane.device

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.dashlane.util.MD5Hash
import com.dashlane.util.deviceCountry
import com.dashlane.util.generateUniqueIdentifier
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DeviceInfoRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val telephonyManagerProvider: Provider<TelephonyManager>
) : DeviceInfoRepository {

    companion object {
        private const val DEVICEID_FILENAME = "dev.id"
        private const val ANONYMOUS_DEVICEID_FILENAME = "adev.id"
    }

    private val telephonyManager
        get() = telephonyManagerProvider.get()

    override val deviceCountry: String
        get() = telephonyManager.deviceCountry
            ?: Locale.getDefault().country.lowercase()

    override val deviceId: String?
        by lazy { readDeviceId(DEVICEID_FILENAME) }

    override val anonymousDeviceId: String?
        by lazy { readDeviceId(ANONYMOUS_DEVICEID_FILENAME) }

    override val deviceName: String
        get() = createDeviceName()

    @Synchronized
    private fun readDeviceId(filename: String): String? {
        val filesDir = context.filesDir
        if (!filesDir.exists() && !filesDir.mkdirs()) {
            
            return null
        }
        val file = File(filesDir, filename)
        if (file.exists()) {
            val deviceId = try {
                file.readText().takeIf { it.isNotEmpty() }
            } catch (e: IOException) {
                null
            }

            if (deviceId == null) {
                file.delete()
            } else {
                return deviceId
            }
        }
        return installDeviceId(file)
    }

    private fun installDeviceId(file: File): String {
        val deviceId = MD5Hash.hash(generateUniqueIdentifier())
        file.sink().buffer().use { it.writeUtf8(deviceId) }
        return deviceId
    }

    private fun createDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            manufacturer.capitalize() + " " + model
        }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}
