package com.dashlane.ui.activities.debug

import android.content.Context
import androidx.preference.PreferenceGroup
import com.dashlane.cryptography.decodeBase64ToUtf8String
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.usercryptography.UserCryptographyRepository
import com.dashlane.util.MD5Hash
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class DebugCategoryCryptography @Inject constructor(
    @ActivityContext override val context: Context,
    val sessionManager: SessionManager,
    userCryptographyRepository: UserCryptographyRepository
) : AbstractDebugCategory() {

    private val session: Session
        get() = sessionManager.session!!

    private val cryptoUserPayload = userCryptographyRepository.getCryptographyMarker(session)?.value ?: ""
    private val userLogin = session.userId

    private val localKeyCipherPayload: String?
        get() {
            val storeName = MD5Hash.hash(userLogin) + ".mp"
            val prefs =
                context.applicationContext.getSharedPreferences(storeName, Context.MODE_PRIVATE)
            val base64 = prefs.getString("lk", null) ?: return null
            return base64.decodeBase64ToUtf8String().substring(0, 50)
        }

    override val name: String
        get() = "Cryptography Status"

    override fun addSubItems(group: PreferenceGroup) {
        addPreferenceButton(group, "CryptoUserPayload:", cryptoUserPayload, null)

        addPreferenceButton(group, "Local Key cryptography payload:", localKeyCipherPayload, null)
    }
}
