package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.m2w.ui.M2wIntroScreen
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.startActivityForResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class M2wIntroActivity : DashlaneActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setCurrentPageView(AnyPage.IMPORT_COMPUTER)

        setContent {
            DashlaneTheme {
                M2wIntroScreen(
                    onStartClick = ::onStartClick,
                    onSkipClick = ::onSkipClick,
                )
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun onStartClick() {
        startActivityForResult<M2wConnectActivity>(M2wConnectActivity.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == M2wConnectActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(resultCode, data)
            finish()
        }
    }

    private fun onSkipClick() {
        val intent = Intent().apply { putExtra(EXTRA_SKIP, true) }
        setResult(Activity.RESULT_OK, intent)
        finish()
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