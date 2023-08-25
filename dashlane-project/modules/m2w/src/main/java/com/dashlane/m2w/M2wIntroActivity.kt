package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.util.setCurrentPageView

class M2wIntroActivity : DashlaneActivity() {

    private lateinit var presenter: M2wIntroPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val origin: String
        try {
            origin = M2wIntentCoordinator.getOrigin(intent)
        } catch (e: IllegalStateException) {
            finish()
            return
        }

        setCurrentPageView(AnyPage.IMPORT_COMPUTER)

        val currentSessionUsageLogRepository = UserActivityComponent(this)
            .currentSessionUsageLogRepository

        presenter = M2wIntroPresenter().also {
            it.logger = M2wIntroLoggerImpl(currentSessionUsageLogRepository, origin)
            it.origin = origin
        }
        presenter.setView(IntroScreenViewProxy(this))

        if (savedInstanceState == null) {
            presenter.logger?.logLand()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.logger?.logBack()
                finish()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == M2wConnectActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(resultCode, data)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.getBooleanExtra(EXTRA_FINISH, false)) {
            finish()
        }
    }

    companion object {
        const val EXTRA_FINISH = "finish"

        const val EXTRA_SKIP = "skip"
    }
}