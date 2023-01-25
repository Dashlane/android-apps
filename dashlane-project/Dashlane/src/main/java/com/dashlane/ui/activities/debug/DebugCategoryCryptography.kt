package com.dashlane.ui.activities.debug

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceGroup
import com.dashlane.cryptography.decodeBase64ToUtf8String
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.Session
import com.dashlane.util.MD5Hash



internal class DebugCategoryCryptography(debugActivity: Activity, session: Session) :
    AbstractDebugCategory(debugActivity) {

    private val cryptoUserPayload =
        SingletonProvider.getComponent().userCryptographyRepository.getCryptographyMarker(session)?.value ?: ""
    private val userLogin = session.userId

    private val localKeyCipherPayload: String?
        get() {
            val storeName = MD5Hash.hash(userLogin) + ".mp"
            val prefs = SingletonProvider.getContext().getSharedPreferences(storeName, Context.MODE_PRIVATE)
            val base64 = prefs.getString("lk", null) ?: return null
            return base64.decodeBase64ToUtf8String().substring(0, 50)
        }

    internal override fun getName(): String {
        return "Cryptography Status"
    }

    internal override fun addSubItems(group: PreferenceGroup) {
        addPreferenceButton(group, "CryptoUserPayload:", cryptoUserPayload, null)

        addPreferenceButton(group, "Local Key cryptography payload:", localKeyCipherPayload, null)
    }
}
