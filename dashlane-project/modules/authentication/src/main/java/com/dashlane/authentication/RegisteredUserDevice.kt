package com.dashlane.authentication

import android.os.Parcel
import android.os.Parcelable
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.asEncryptedBase64
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.time.Instant



sealed class RegisteredUserDevice : Parcelable {
    abstract val login: String
    abstract val securityFeatures: Set<SecurityFeature>
    abstract val serverKey: String?

    

    val isServerKeyRequired
        get() = serverKey != null

    

    @Parcelize
    data class ToRestore(
        override val login: String,
        override val securityFeatures: Set<SecurityFeature>,
        val cipheredBackupToken: String
    ) : RegisteredUserDevice() {
        override val serverKey: String?
            get() = null
    }

    

    @Parcelize
    data class Local(
        override val login: String,
        override val securityFeatures: Set<SecurityFeature>,
        override val serverKey: String? = null,
        val accessKey: String
    ) : RegisteredUserDevice()

    

    @Parcelize
    data class Remote(
        override val login: String,
        override val securityFeatures: Set<SecurityFeature>,
        override val serverKey: String? = null,
        val accessKey: String,
        val secretKey: String,
        val encryptedSettings: String,
        val settingsDate: Instant,
        val sharingKeys: SharingKeys?,
        val userId: String,
        val hasDesktopDevice: Boolean,
        val registeredDeviceCount: Long,
        val deviceAnalyticsId: String,
        val userAnalyticsId: String,
        val encryptedRemoteKey: @WriteWith<EncryptedBase64StringParceler> EncryptedBase64String? = null,
        val registeredWithBackupToken: Boolean = false
    ) : RegisteredUserDevice() {
        @Parcelize
        data class SharingKeys(
            val publicKey: String,
            val encryptedPrivateKey: String
        ) : Parcelable

        internal object EncryptedBase64StringParceler : Parceler<EncryptedBase64String?> {
            override fun create(parcel: Parcel): EncryptedBase64String? =
                parcel.readString()?.asEncryptedBase64()

            override fun EncryptedBase64String?.write(parcel: Parcel, flags: Int) {
                parcel.writeString(this?.value)
            }
        }
    }
}
