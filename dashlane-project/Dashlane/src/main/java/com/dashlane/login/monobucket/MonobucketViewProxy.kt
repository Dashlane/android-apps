package com.dashlane.login.monobucket

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.LoginIntents
import com.dashlane.login.progress.LoginSyncProgressActivity
import com.dashlane.premium.offer.list.view.OfferListFragment
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.startActivity
import kotlinx.coroutines.launch

class MonobucketViewProxy(
    private val activity: FragmentActivity,
    private val viewModel: MonobucketViewModelContract
) {
    private val confirmFragmentTag = MonobucketUnregisterDeviceFragment::class.java.name

    init {
        if (viewModel.hasSync()) {
            activity.startActivity(LoginIntents.createSettingsActivityIntent(activity))
            activity.finish()
        }
        activity.findViewById<TextView>(R.id.text_title).setText(R.string.login_monobucket_title)
        activity.findViewById<TextView>(R.id.text_subtitle).setText(R.string.login_monobucket_subtitle)
        activity.findViewById<Button>(R.id.negative_button).apply {
            setText(R.string.login_monobucket_unlink_previous_device)
            setOnClickListener {
                unlinkPreviousDevice()
            }
        }
        activity.findViewById<Button>(R.id.top_left_button).apply {
            setText(R.string.login_monobucket_log_out)
            setOnClickListener {
                viewModel.onLogOut()
            }
        }
        activity.findViewById<Button>(R.id.positive_button).apply {
            setText(R.string.login_monobucket_see_premium_plan)
            setOnClickListener {
                upgradeToPremium()
            }
        }
        collectState()
    }

    private fun collectState() {
        activity.lifecycle.coroutineScope.launch {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    when (it) {
                        MonobucketState.CanceledUnregisterDevice -> {
                            activity.setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT)
                        }
                        MonobucketState.ConfirmUnregisterDevice -> {
                            
                            activity.intent.putExtra(LoginSyncProgressActivity.EXTRA_MONOBUCKET_UNREGISTRATION, true)
                            val intent = LoginIntents.createSettingsActivityIntent(activity)
                            activity.startActivity(intent)
                        }
                        MonobucketState.UserLoggedOut -> {
                            activity.startActivity(LoginIntents.createLoginActivityIntent(activity))
                        }
                        MonobucketState.Idle -> {
                            
                        }
                    }
                }
            }
        }
    }

    private fun unlinkPreviousDevice() {
        viewModel.onUnlinkPreviousDevice()
        showConfirmUnregisterDevice()
        activity.setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT_UNLINK_DEVICE)
    }

    private fun upgradeToPremium() {
        viewModel.onUpgradePremium()
        activity.startActivity<OffersActivity> {
            putExtra(OfferListFragment.EXTRA_ORIGIN, OfferListFragment.ORIGIN_MONOBUCKET)
        }
    }

    private fun showConfirmUnregisterDevice() {
        MonobucketUnregisterDeviceFragment().show(
            activity.supportFragmentManager,
            confirmFragmentTag
        )
    }
}