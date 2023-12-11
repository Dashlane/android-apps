package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class M2wIntroActivity : DashlaneActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var presenter: M2wIntroPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        setCurrentPageView(AnyPage.IMPORT_COMPUTER)

        presenter = M2wIntroPresenter()
        presenter.setView(IntroScreenViewProxy(this))

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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