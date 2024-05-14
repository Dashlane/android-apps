package com.dashlane.m2w

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.coroutineScope
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class M2wConnectActivity : DashlaneActivity() {

    private val viewModel by viewModels<M2wConnectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentPageView(AnyPage.TOOLS_NEW_DEVICE)
        setContentView(R.layout.activity_m2w_connect)
        supportActionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_up_indicator_close)
            setDisplayHomeAsUpEnabled(true)
        }

        M2wConnectViewProxy(this, viewModel, lifecycle.coroutineScope)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                viewModel.finishM2w(M2WResult.CANCELLED)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val REQUEST_CODE = 5950
    }
}