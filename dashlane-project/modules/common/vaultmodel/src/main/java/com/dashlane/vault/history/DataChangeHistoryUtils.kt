package com.dashlane.vault.history

import android.os.Build
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

private const val EMAIL = "Email"
private const val LOGIN = "Login"
private const val NOTE = "Note"
private const val PASSWORD = "Password"
private const val TITLE = "Title"
private const val URL = "Url"
private const val USER_SELECTED_URL = "UserSelectedUrl"
private const val OTP_SECRET = "OtpSecret"
private const val OTP_URL = "OtpUrl"

fun VaultItem<SyncObject.Authentifiant>.toChangeSet(
    oldAuthentifiant: SyncObject.Authentifiant? = null,
    userName: String,
    removed: Boolean = this.isDeleted()
): SyncObject.DataChangeHistory.ChangeSet {
    val authentifiant = syncObject
    val changedProperties = ArrayList<String>()
    if (authentifiant.title != oldAuthentifiant?.title) changedProperties.add(TITLE)
    if (authentifiant.email != oldAuthentifiant?.email) changedProperties.add(EMAIL)
    if (authentifiant.login != oldAuthentifiant?.login) changedProperties.add(LOGIN)
    if (authentifiant.password != oldAuthentifiant?.password) changedProperties.add(PASSWORD)
    if (authentifiant.otpSecret != oldAuthentifiant?.otpSecret) changedProperties.add(OTP_SECRET)
    if (authentifiant.otpUrl != oldAuthentifiant?.otpUrl) changedProperties.add(OTP_URL)
    if (authentifiant.note != oldAuthentifiant?.note) changedProperties.add(NOTE)
    if (authentifiant.url != oldAuthentifiant?.url) changedProperties.add(URL)
    if (authentifiant.userSelectedUrl != oldAuthentifiant?.userSelectedUrl) changedProperties.add(USER_SELECTED_URL)

    val changeSetBuilder = SyncObject.DataChangeHistory.ChangeSet.Builder()
        .apply {
            user = userName
            deviceName = Build.MODEL
            modificationDate = Instant.now()
            platform = SyncObject.Platform.SERVER_ANDROID
            this.removed = removed
            this.changedProperties = changedProperties
            currentData = authentifiant.toPropertyValueMap()
            id = generateUniqueIdentifier()
        }
    return changeSetBuilder.build()
}

fun SyncObject.Authentifiant.toPropertyValueMap(): Map<String, String> = mapOf(
    TITLE to (this.title ?: ""),
    EMAIL to (this.email ?: ""),
    LOGIN to (this.login ?: ""),
    PASSWORD to (this.password?.toString() ?: ""),
    OTP_SECRET to (this.otpSecret?.toString() ?: ""),
    OTP_URL to (this.otpUrl?.toString() ?: ""),
    NOTE to (this.note ?: ""),
    URL to (this.url ?: ""),
    USER_SELECTED_URL to (this.userSelectedUrl ?: "")
)


val SyncObject.DataChangeHistory.ChangeSet.email: String? get() = currentData?.get(EMAIL)
val SyncObject.DataChangeHistory.ChangeSet.login: String? get() = currentData?.get(LOGIN)
val SyncObject.DataChangeHistory.ChangeSet.note: String? get() = currentData?.get(NOTE)
val SyncObject.DataChangeHistory.ChangeSet.password: String? get() = currentData?.get(PASSWORD)
val SyncObject.DataChangeHistory.ChangeSet.title: String? get() = currentData?.get(TITLE)
val SyncObject.DataChangeHistory.ChangeSet.url: String? get() = currentData?.get(URL)
val SyncObject.DataChangeHistory.ChangeSet.userSelectedUrl: String? get() = currentData?.get(USER_SELECTED_URL)
