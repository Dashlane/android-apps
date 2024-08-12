package com.dashlane.guidedonboarding.darkwebmonitoring

import android.annotation.SuppressLint
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
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingDarkWebMonitoringActivity :
    DashlaneActivity(),
    OnboardingDarkWebMonitoringErrorFragment.Listener {
    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var darkWebMonitoringManager: DarkWebMonitoringManager

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope

    @Inject
    lateinit var sessionManager: SessionManager

    private val navController: NavController
        get() = findNavController(R.id.nav_host_onboarding_dark_web)
    private val currentAccountEmail by lazy {
        globalPreferencesManager.getLastLoggedInUser()
    }
    private var checkingDone = false
    private var emailConfirmed = false
    private val resultIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_dark_web_monitoring)
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

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
        if (checkingDone) {
            finish()
        }
    }

    override fun onTryAgain() {
        checkingDone = false
        emailConfirmed = false
        navController.navigate(goToLoading())
        checkDarkWebStatus()
    }

    override fun onSkip() {
        updateActivityResult(RESULT_CANCELED)
        finish()
    }

    private fun checkDarkWebStatus() = applicationCoroutineScope.launch(Dispatchers.Main.immediate) {
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
                updateActivityResult(RESULT_OK)
                showSuccess(hasAlerts)
            }
            else -> showError()
        }
        checkingDone = true
    }

    private fun showSuccess(hasAlerts: Boolean) {
        if (!hasAlerts) {
            if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_success_no_alerts) return
            navController.navigate(loadingToSuccessNoAlerts())
        } else {
            if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_success_email_confirmed) return
            navController.navigate(loadingToSuccessEmailConfirmed())
        }
    }

    private fun showError() {
        if (navController.currentDestination?.id == R.id.nav_onboarding_dwm_error) return
        navController.navigate(loadingToError())
    }

    private fun updateActivityResult(result: Int) {
        setResult(result, resultIntent)
    }

    companion object {
        
        private val MIN_LOADING_TIME = Duration.ofMillis(800)
    }
}
