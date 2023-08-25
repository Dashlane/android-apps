package com.dashlane.login.monobucket

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonobucketActivity : DashlaneActivity() {

    private val viewModel: MonobucketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning)

        MonobucketViewProxy(this, viewModel)

        if (savedInstanceState == null) {
            viewModel.onShow()
            setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                
            }
        }
        )
    }

    companion object {
        const val EXTRA_BUCKET_OWNER = "bucket_owner"
    }
}
