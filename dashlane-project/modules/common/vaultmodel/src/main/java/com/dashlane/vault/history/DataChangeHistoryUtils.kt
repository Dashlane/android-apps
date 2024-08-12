package com.dashlane.vault.history

import android.os.Build
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.history.DataChangeHistoryField.EMAIL
import com.dashlane.vault.history.DataChangeHistoryField.LOGIN
import com.dashlane.vault.history.DataChangeHistoryField.NOTE
import com.dashlane.vault.history.DataChangeHistoryField.OTP_SECRET
import com.dashlane.vault.history.DataChangeHistoryField.OTP_URL
import com.dashlane.vault.history.DataChangeHistoryField.PASSWORD
import com.dashlane.vault.history.DataChangeHistoryField.TITLE
import com.dashlane.vault.history.DataChangeHistoryField.URL
import com.dashlane.vault.history.DataChangeHistoryField.USER_SELECTED_URL
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

fun VaultItem<SyncObject.Authentifiant>.toChangeSet(
    oldAuthentifiant: SyncObject.Authentifiant? = null,
    userName: String,
    removed: Boolean = this.isDeleted()
): SyncObject.DataChangeHistory.ChangeSet {
    val authentifiant = syncObject
    val changedProperties = ArrayList<String>()
    if (authentifiant.title != oldAuthentifiant?.title) changedProperties.add(TITLE.field)
    if (authentifiant.email != oldAuthentifiant?.email) changedProperties.add(EMAIL.field)
    if (authentifiant.login != oldAuthentifiant?.login) changedProperties.add(LOGIN.field)
    if (authentifiant.password != oldAuthentifiant?.password) changedProperties.add(PASSWORD.field)
    if (authentifiant.otpSecret != oldAuthentifiant?.otpSecret) changedProperties.add(OTP_SECRET.field)
    if (authentifiant.otpUrl != oldAuthentifiant?.otpUrl) changedProperties.add(OTP_URL.field)
    if (authentifiant.note != oldAuthentifiant?.note) changedProperties.add(NOTE.field)
    if (authentifiant.url != oldAuthentifiant?.url) changedProperties.add(URL.field)
    if (authentifiant.userSelectedUrl != oldAuthentifiant?.userSelectedUrl) changedProperties.add(USER_SELECTED_URL.field)

    val changeSetBuilder = SyncObject.DataChangeHistory.ChangeSet.Builder()
        .apply {
            user = userName
            deviceName = Build.MODEL
            modificationDate = Instant.now()
            platform = SyncObject.Platform.SERVER_ANDROID
            this.removed = removed
            this.changedProperties = changedProperties
            currentData = oldAuthentifiant?.toPropertyValueMap()
            id = generateUniqueIdentifier()
        }
    return changeSetBuilder.build()
}

fun SyncObject.Authentifiant.toPropertyValueMap(): Map<String, String> = mapOf(
    TITLE.field to (this.title ?: ""),
    EMAIL.field to (this.email ?: ""),
    LOGIN.field to (this.login ?: ""),
    PASSWORD.field to (this.password?.toString() ?: ""),
    OTP_SECRET.field to (this.otpSecret?.toString() ?: ""),
    OTP_URL.field to (this.otpUrl?.toString() ?: ""),
    NOTE.field to (this.note ?: ""),
    URL.field to (this.url ?: ""),
    USER_SELECTED_URL.field to (this.userSelectedUrl ?: "")
)


val SyncObject.DataChangeHistory.ChangeSet.email: String? get() = currentData?.get(EMAIL.field)
val SyncObject.DataChangeHistory.ChangeSet.login: String? get() = currentData?.get(LOGIN.field)
val SyncObject.DataChangeHistory.ChangeSet.note: String? get() = currentData?.get(NOTE.field)
val SyncObject.DataChangeHistory.ChangeSet.password: String? get() = currentData?.get(PASSWORD.field)
val SyncObject.DataChangeHistory.ChangeSet.title: String? get() = currentData?.get(TITLE.field)
val SyncObject.DataChangeHistory.ChangeSet.url: String? get() = currentData?.get(URL.field)
val SyncObject.DataChangeHistory.ChangeSet.userSelectedUrl: String? get() = currentData?.get(USER_SELECTED_URL.field)
