package com.dashlane.device

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.MD5Hash
import com.dashlane.util.deviceCountry
import com.dashlane.util.generateUniqueIdentifier
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import okio.buffer
import okio.sink



@Singleton
class DeviceInfoRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val telephonyManagerProvider: Provider<TelephonyManager>
) : DeviceInfoRepository {

    companion object {
        private const val DEVICEID_FILENAME = "dev.id"
        private const val ANONYMOUS_DEVICEID_FILENAME = "adev.id"
    }

    private val telephonyManager
        get() = telephonyManagerProvider.get()

    override var deviceCountry: String
        get() = globalPreferencesManager.getDeviceCountry()
            ?: telephonyManager.deviceCountry
            ?: Locale.getDefault().country.lowercase()
        set(value) = globalPreferencesManager.setDeviceCountry(value)

    override var inEuropeanUnion: Boolean
        get() = globalPreferencesManager.getDeviceInEuropeanUnion()
        set(value) = globalPreferencesManager.setDeviceInEuropeanUnion(value)

    override val deviceCountryRefreshTimestamp: Long
        get() = globalPreferencesManager.getDeviceCountryRefreshTimestamp()

    override val deviceId
            by lazy { readDeviceId(DEVICEID_FILENAME) }

    override val anonymousDeviceId
            by lazy { readDeviceId(ANONYMOUS_DEVICEID_FILENAME) }

    override val deviceName: String
        get() = createDeviceName()

    private fun readDeviceId(filename: String): String {
        val filesDir = context.filesDir
        if (!filesDir.exists()) {
            filesDir.mkdirs()
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
        var deviceId = MD5Hash.hash(generateUniqueIdentifier())
        file.sink().buffer().use { it.writeUtf8(deviceId) }
        return deviceId
    }

    private fun createDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }
}
