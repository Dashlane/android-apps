package com.dashlane.login.monobucket

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.performancelogger.TimeToLoadLocalLogger
import com.dashlane.performancelogger.TimeToLoadRemoteLogger
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonobucketActivity : DashlaneActivity() {

    @Inject
    lateinit var timeToLoadRemoteLogger: TimeToLoadRemoteLogger

    @Inject
    lateinit var timeToLoadLocalLogger: TimeToLoadLocalLogger

    private val viewModel: MonobucketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning)

        MonobucketViewProxy(this, viewModel)

        if (savedInstanceState == null) {
            viewModel.onShow()
            setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                
            }
        })
    }

    override fun onStart() {
        super.onStart()

        clearLoadAccountLogger()
    }

    private fun clearLoadAccountLogger() {
        timeToLoadLocalLogger.clear()
        timeToLoadRemoteLogger.clear()
    }

    companion object {
        const val EXTRA_BUCKET_OWNER = "bucket_owner"
    }
}
