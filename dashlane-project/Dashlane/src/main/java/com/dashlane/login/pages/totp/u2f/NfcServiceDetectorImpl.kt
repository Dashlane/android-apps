package com.dashlane.login.pages.totp.u2f

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dashlane.item.nfc.NfcHelper
import com.dashlane.util.getParcelableExtraCompat
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

@ActivityScoped
class NfcServiceDetectorImpl @Inject constructor(
    private val activity: Activity
) : NfcServiceDetector, LifecycleEventObserver {

    override val isNfcAvailable: Boolean
        get() = NfcHelper.isNfcAvailable(activity)

    private val nfcHelper = NfcHelper(activity)

    var callback by Delegates.observable<((IsoDep) -> Unit)?>(null) { _, _, _ -> refreshNfcListening() }

    private var resumed by Delegates.observable(false) { _, _, _ -> refreshNfcListening() }
    private var listening = false

    override suspend fun detectNfcTag() =
        suspendCoroutine<IsoDep> { continuation ->
            detectTag {
                this.callback = null
                continuation.resume(it)
            }
        }

    private fun detectTag(callback: (IsoDep) -> Unit) {
        this.callback = callback
        refreshNfcListening()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            resumed = true
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            resumed = false
        }
    }

    fun onNewIntent(intent: Intent) {
        val tag = intent.getParcelableExtraCompat<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            callback?.invoke(IsoDep.get(tag))
        }
    }

    private fun refreshNfcListening() {
        val shouldListen = callback != null && resumed
        if (shouldListen && !listening) {
            register()
        } else if (!shouldListen && listening) {
            unregister()
        }
    }

    private fun register() {
        nfcHelper.enableDispatch()
        listening = true
    }

    private fun unregister() {
        nfcHelper.disableDispatch()
        listening = false
    }
}