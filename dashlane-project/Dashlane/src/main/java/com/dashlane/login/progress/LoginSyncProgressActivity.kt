package com.dashlane.login.progress

import android.os.Bundle
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.login.Device
import com.dashlane.login.LoginIntents
import com.dashlane.login.pages.password.InitialSync
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.coroutines.getDeferredViewModel



class LoginSyncProgressActivity : DashlaneActivity() {

    private lateinit var loginSyncProgressPresenter: LoginSyncProgressPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sync_progress)

        val extras = intent.extras
        val isMonobucketUnregistration = extras != null && extras.getBoolean(EXTRA_MONOBUCKET_UNREGISTRATION)
        val devicesToUnregister =
            extras?.getParcelableArray(EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION)?.map { it as Device } ?: emptyList()

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
        val session = SingletonProvider.getSessionManager().session
        if (session == null) {
            
            startActivity(LoginIntents.createLoginActivityIntent(this))
            finish()
            return
        }
        loginSyncProgressPresenter = LoginSyncProgressPresenter(
            this,
            session,
            viewProxy,
            InitialSync(session, SingletonProvider.getDataSync().syncHelper),
            devicesToUnregister,
            SingletonProvider.getComponent().dashlaneApi.endpoints.devices.deactivateDevicesService,
            viewModel,
            lifecycleScope
        ).also { viewProxy.presenter = it }
    }

    override fun onBackPressed() {
        
    }

    companion object {
        private const val VIEW_MODEL_TAG_SYNC = "sync"

        

        const val EXTRA_MONOBUCKET_UNREGISTRATION = "monobucket_unregistration"

        

        const val EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION = "extra_device_sync_limit_unregistration"
    }
}
