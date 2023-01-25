package com.dashlane.guidedonboarding.darkwebmonitoring

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dashlane.darkweb.DarkWebEmailStatus.Companion.STATUS_ACTIVE
import com.dashlane.darkweb.DarkWebMonitoringManager
import com.dashlane.guidedonboarding.OnboardingDarkWebMonitoringNavigationDirections.Companion.goToLoading
import com.dashlane.guidedonboarding.R
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringLoadingFragmentDirections.Companion.loadingToError
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringLoadingFragmentDirections.Companion.loadingToSuccessEmailConfirmed
import com.dashlane.guidedonboarding.darkwebmonitoring.OnboardingDarkWebMonitoringLoadingFragmentDirections.Companion.loadingToSuccessNoAlerts
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.inject.UserActivityComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject



@AndroidEntryPoint
class OnboardingDarkWebMonitoringActivity : DashlaneActivity(),
    OnboardingDarkWebMonitoringErrorFragment.Listener {
    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var darkWebMonitoringManager: DarkWebMonitoringManager

    private val navController: NavController
        get() = findNavController(R.id.nav_host_onboarding_dark_web)
    private val currentAccountEmail by lazy {
        globalPreferencesManager.getLastLoggedInUser()
    }
    private var checkingDone = false
    private var emailConfirmed = false
    private val resultIntent = Intent()
    private var logger: OnboardingDarkWebUsageLogger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_dark_web_monitoring)
        logger =
            OnboardingDarkWebUsageLogger(UserActivityComponent(this).currentSessionUsageLogRepository)
    }

    override fun onResume() {
        super.onResume()
        if (!checkingDone) {
            
            
            if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                navController.navigate(goToLoading())
            }
            checkDarkWebStatus()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        
        if (checkingDone) {
            finish()
        }
    }

    override fun onTryAgain() {
        checkingDone = false
        emailConfirmed = false
        logger?.logTryAgain()
        navController.navigate(goToLoading())
        checkDarkWebStatus()
    }

    override fun onSkip() {
        logger?.logSkip()
        finish()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkDarkWebStatus() = GlobalScope.launch(Dispatchers.Main.immediate) {
        val startTime = Instant.now()
        checkingDone = false
        darkWebMonitoringManager.invalidateCache()
        val emails = darkWebMonitoringManager.getEmailsWithStatus()
        val matchingEmail = emails?.firstOrNull { it.email == currentAccountEmail }
        updateActivityResult(RESULT_CANCELED)
        val remainingLoadingTime = MIN_LOADING_TIME - Duration.between(startTime, Instant.now())
        if (!remainingLoadingTime.isNegative) delay(remainingLoadingTime.toMillis())
        when {
            matchingEmail == null -> showError()
            matchingEmail.status == STATUS_ACTIVE -> {
                emailConfirmed = true
                
                val alerts = darkWebMonitoringManager.getBreaches(0L)
                val hasAlerts = alerts?.second?.isNotEmpty() == true
                updateActivityResult(RESULT_OK, hasAlerts)
                showSuccess(hasAlerts)
            }
            else -> showError()
        }
        checkingDone = true
    }

    private fun showSuccess(hasAlerts: Boolean) {
        if (!hasAlerts) {
            if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_success_no_alerts) return
            logger?.logNoAlerts()
            navController.navigate(loadingToSuccessNoAlerts())
        } else {
            if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_success_email_confirmed) return
            logger?.logEmailVerified()
            navController.navigate(loadingToSuccessEmailConfirmed())
        }
    }

    private fun showError() {
        if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_error) return
        logger?.logEmailUnverified()
        navController.navigate(loadingToError())
    }

    private fun updateActivityResult(result: Int, hasAlerts: Boolean = true) {
        resultIntent.putExtra(EXTRA_HAS_ALERTS, hasAlerts)
        setResult(result, resultIntent)
    }

    companion object {
        const val EXTRA_HAS_ALERTS = "email_has_alerts"

        
        private val MIN_LOADING_TIME = Duration.ofMillis(800)
    }
}