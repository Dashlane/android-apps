package com.dashlane.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.authentication.sso.GetUserSsoInfoActivity.Companion.createUserSsoInfoHandlingIntent
import com.dashlane.debug.services.DaDaDaTheme
import com.dashlane.design.theme.color.DebugTheme
import com.dashlane.navigation.NavigationConstants
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.SplashScreenIntentFactory.Companion.create
import com.dashlane.util.findContentParent
import com.dashlane.util.log.FirstLaunchDetector
import com.dashlane.util.log.LaunchLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : FragmentActivity() {

    @Inject
    lateinit var firstLaunchDetector: FirstLaunchDetector

    @Inject
    lateinit var launchLogger: LaunchLogger

    @Inject
    lateinit var syncBroadcastManager: SyncBroadcastManager

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionCredentialsSaver: SessionCredentialsSaver

    @Inject
    lateinit var dadadaTheme: DaDaDaTheme

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepSplashScreenTheme()
        if (interceptUserSsoInfo()) {
            finish()
            return
        }
        proceedWithLoading()
        if (savedInstanceState == null) {
            launchLogger.logLaunched()
        }

        DebugTheme.enabled = dadadaTheme.hasDebugTheme
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        firstLaunchDetector.detect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        
        
    }

    private fun proceedWithLoading() {
        syncBroadcastManager.removePasswordBroadcastIntent()
        val lastUser = globalPreferencesManager.getDefaultUsername()
        if (lastUser != null) {
            globalPreferencesManager.saveSkipIntro()
        }
        startNextActivity()
    }

    private fun startNextActivity() {
        val intentFactory = create(
            this,
            globalPreferencesManager,
            preferencesManager,
            sessionManager,
            sessionCredentialsSaver,
        )
        val newIntent = intentFactory.createIntent()
        newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        val currentIntent = intent
        newIntent.putExtra(NavigationConstants.STARTED_WITH_INTENT, currentIntent)
        if (currentIntent != null) {
            val uri = currentIntent.data
            if (uri != null) {
                
                startActivity(newIntent)
                finishAffinity()
                return
            }
        }
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        ).toBundle()
        startActivity(newIntent, options)
        finishAffinity()
    }

    private fun interceptUserSsoInfo(): Boolean {
        val intent = intent
        val data = intent.data
        if (data == null || "ssologin" != data.host) {
            return false
        }
        var flags = intent.flags
        if (flags and Intent.FLAG_ACTIVITY_NEW_TASK == 0 ||
            flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0
        ) {
            
            
            
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            val newIntent = Intent(this, SplashScreenActivity::class.java)
                .setData(data)
                .setFlags(flags)
            startActivity(newIntent)
        } else {
            startActivity(createUserSsoInfoHandlingIntent(this, data))
        }
        return true
    }

    private fun keepSplashScreenTheme() {
        val started = booleanArrayOf(false)
        this.findContentParent()
            .viewTreeObserver
            .addOnPreDrawListener {
                if (!started[0]) {
                    started[0] = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                }
                started[0]
            }
    }
}