package com.dashlane.login.progress

import android.os.Bundle
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.login.Device
import com.dashlane.login.LoginIntents
import com.dashlane.login.pages.password.InitialSync
import com.dashlane.server.api.DashlaneApi
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.getParcelableArrayCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginSyncProgressActivity : DashlaneActivity() {

    @Inject
    lateinit var initialSync: InitialSync

    @Inject
    lateinit var dashlaneApi: DashlaneApi

    @Inject
    lateinit var syncBroadcastManager: SyncBroadcastManager

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var loginSyncProgressPresenter: LoginSyncProgressPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sync_progress)

        val extras = intent.extras
        val isMonobucketUnregistration = extras != null && extras.getBoolean(EXTRA_MONOBUCKET_UNREGISTRATION)
        val devicesToUnregister =
            extras?.getParcelableArrayCompat<Device>(EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION) ?: emptyList()

        val initialMessage = when {
            savedInstanceState != null -> getString(R.string.login_sync_progress_deciphering)
            isMonobucketUnregistration ->
                
                
                
                getString(R.string.login_sync_progress_monobucket_unregister)
            devicesToUnregister.isNotEmpty() ->
                
                getString(R.string.login_sync_progress_device_limit_unregister)
            else -> getString(R.string.login_sync_progress_deciphering)
        }
        val viewProxy = LoginSyncProgressViewProxy(this, initialMessage, lifecycleScope)
        val viewModel = ViewModelProvider(this).getDeferredViewModel<Unit>(VIEW_MODEL_TAG_SYNC)
        val session = sessionManager.session
        if (session == null) {
            
            startActivity(LoginIntents.createLoginActivityIntent(this))
            finish()
            return
        }
        loginSyncProgressPresenter = LoginSyncProgressPresenter(
            activity = this,
            sessionManager = sessionManager,
            viewProxy = viewProxy,
            initialSync = initialSync,
            devicesToUnregister = devicesToUnregister,
            deactivateDevicesService = dashlaneApi.endpoints.devices.deactivateDevicesService,
            viewModel = viewModel,
            syncBroadcastManager = syncBroadcastManager,
            coroutineScope = lifecycleScope
        ).also { viewProxy.presenter = it }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    
                }
            }
        )
    }

    companion object {
        private const val VIEW_MODEL_TAG_SYNC = "sync"

        const val EXTRA_MONOBUCKET_UNREGISTRATION = "monobucket_unregistration"

        const val EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION = "extra_device_sync_limit_unregistration"
    }
}
