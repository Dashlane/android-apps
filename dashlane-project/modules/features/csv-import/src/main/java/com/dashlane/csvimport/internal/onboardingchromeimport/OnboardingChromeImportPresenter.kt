package com.dashlane.csvimport.internal.onboardingchromeimport

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dashlane.csvimport.ImportFromChromeLogger
import com.dashlane.csvimport.OnboardingChromeImportActivity
import com.dashlane.csvimport.OnboardingChromeImportUtils
import com.dashlane.csvimport.internal.Intents
import com.dashlane.csvimport.internal.localBroadcastManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.skocken.presentation.presenter.BasePresenter

internal class OnboardingChromeImportPresenter(
    private val origin: String?,
    private val userPreferencesManager: UserPreferencesManager,
    usageLogRepository: UsageLogRepository?
) : BasePresenter<OnboardingChromeImportContract.DataProvider, OnboardingChromeImportContract.ViewProxy>(),
    OnboardingChromeImportContract.Presenter {

    private val logger = ImportFromChromeLogger(usageLogRepository, originStr = origin)

    private val csvImportReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            
            
            with(context) {
                startActivity(
                    OnboardingChromeImportActivity.newIntent(this, origin)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }

            activity?.run {
                when (intent.getStringExtra(Intents.EXTRA_CSV_IMPORT_RESULT)) {
                    Intents.CSV_IMPORT_RESULT_SUCCESS -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    Intents.CSV_IMPORT_RESULT_FAILURE -> {
                        logger.logOnboardingErrorDisplayed()
                        view.showImportError()
                    }
                    Intents.CSV_IMPORT_RESULT_CANCEL -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val autoPlayRunnable = Runnable {
        if (view.currentIllustration + 1 == view.illustrationCount) {
            stopAutoPlayIfNeeded()
        } else {
            view.currentIllustration++
            postAutoPlayIfNeeded()
        }
    }

    private var autoPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            logger.logOnboardingDisplayed()
        }

        view.currentIllustration = savedInstanceState?.getInt(STATE_CURRENT_ILLUSTRATION) ?: 0
        autoPlay = savedInstanceState?.getBoolean(STATE_AUTO_PLAY) ?: true

        activity?.localBroadcastManager
            ?.registerReceiver(csvImportReceiver, IntentFilter(Intents.ACTION_CSV_IMPORT))
    }

    override fun onResume() {
        postAutoPlayIfNeeded()
    }

    override fun onPause() {
        handler.removeCallbacks(autoPlayRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            outState.putInt(STATE_CURRENT_ILLUSTRATION, view.currentIllustration)
            outState.putBoolean(STATE_AUTO_PLAY, autoPlay)
        }
    }

    override fun onDestroy() {
        activity?.runCatching {
            localBroadcastManager.unregisterReceiver(csvImportReceiver)
        }
    }

    override fun onSwipeLeft() {
        stopAutoPlayIfNeeded()

        with(view) {
            currentIllustration = (currentIllustration + 1).coerceAtMost(illustrationCount - 1)
        }
    }

    override fun onSwipeRight() {
        stopAutoPlayIfNeeded()

        with(view) {
            currentIllustration = (currentIllustration - 1).coerceAtLeast(0)
        }
    }

    override fun onStepClicked(index: Int) {
        stopAutoPlayIfNeeded()

        view.currentIllustration = index
    }

    override fun onImportErrorCanceled() {
        logger.logOnboardingErrorRetryClicked()
    }

    override fun onImportErrorSkipClicked() {
        logger.logOnboardingErrorSkipClicked()
        activity?.run {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onMayBeLaterClicked() {
        logger.logOnboardingSkipClicked()
        activity?.run {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onOpenChromeClicked() {
        stopAutoPlayIfNeeded()
        logger.logOnboardingOpenChromeClicked()
        userPreferencesManager.hasStartedChromeImport = true
        activity?.let(OnboardingChromeImportUtils::launchChrome)
    }

    private fun postAutoPlayIfNeeded() {
        if (autoPlay) {
            handler.postDelayed(autoPlayRunnable, AUTOPLAY_DELAY)
        }
    }

    private fun stopAutoPlayIfNeeded() {
        if (autoPlay) {
            autoPlay = false
            handler.removeCallbacks(autoPlayRunnable)
        }
    }

    companion object {
        private const val STATE_CURRENT_ILLUSTRATION = "current_illustration"
        private const val STATE_AUTO_PLAY = "auto_play"

        private const val AUTOPLAY_DELAY = 3_000L
    }
}