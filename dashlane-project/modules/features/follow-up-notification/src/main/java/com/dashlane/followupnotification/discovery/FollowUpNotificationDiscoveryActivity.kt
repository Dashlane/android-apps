package com.dashlane.followupnotification.discovery

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.followupnotification.R
import com.dashlane.followupnotification.services.FollowUpNotificationDiscoveryService
import com.dashlane.followupnotification.services.FollowUpNotificationLogger
import com.skocken.presentation.presenter.BasePresenter
import com.skocken.presentation.provider.BaseDataProvider
import com.skocken.presentation.viewproxy.BaseViewProxy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowUpNotificationDiscoveryActivity : AppCompatActivity() {
    private lateinit var presenter: Presenter

    @Inject
    lateinit var followUpNotificationLogger: FollowUpNotificationLogger

    @Inject
    lateinit var followUpNotificationDiscoveryService: FollowUpNotificationDiscoveryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = Presenter(
            followUpNotificationLogger,
            followUpNotificationDiscoveryService
        )
        presenter.setProvider(DataProvider())
        presenter.setView(ViewProxy(this))

        val isReminder = intent.extras?.getBoolean(EXTRA_IS_REMINDER) ?: false
        presenter.setupDiscoveryScreen(isReminder)
    }

    private class Presenter(
        private val logger: FollowUpNotificationLogger,
        private val service: FollowUpNotificationDiscoveryService
    ) :
        BasePresenter<FollowUpNotificationDiscoveryContract.DataProvider, FollowUpNotificationDiscoveryContract.ViewProxy>(),
        FollowUpNotificationDiscoveryContract.Presenter {
        override fun setupDiscoveryScreen(isReminder: Boolean) {
            
            
            service.setHasSeenIntroductionScreen()

            val configuration = if (isReminder) {
                ScreenConfiguration.REMINDER
            } else {
                ScreenConfiguration.INTRODUCTION
            }
            view.setScreenContent(provider.getScreenContent(configuration))

            when (configuration) {
                ScreenConfiguration.INTRODUCTION -> {
                    logger.logDisplayDiscoveryIntroduction()
                    view.setOnButtonClick {
                        activity?.finish()
                    }
                }
                ScreenConfiguration.REMINDER -> {
                    logger.logDisplayDiscoveryReminder()
                    view.setOnButtonClick {
                        service.setHasAcknowledgedReminderScreen()
                        activity?.finish()
                    }
                }
            }
        }
    }

    private class DataProvider :
        BaseDataProvider<FollowUpNotificationDiscoveryContract.Presenter>(),
        FollowUpNotificationDiscoveryContract.DataProvider {

        override fun getScreenContent(config: ScreenConfiguration) = when (config) {
            ScreenConfiguration.INTRODUCTION -> ScreenContent(
                false,
                R.string.discovery_screen_title_introduction,
                R.string.discovery_screen_body_introduction
            )
            ScreenConfiguration.REMINDER -> ScreenContent(
                true,
                R.string.discovery_screen_title_reminder,
                R.string.discovery_screen_body_reminder
            )
        }
    }

    private class ViewProxy(private val activity: Activity) :
        BaseViewProxy<FollowUpNotificationDiscoveryContract.Presenter>(activity),
        FollowUpNotificationDiscoveryContract.ViewProxy {

        val swipeIcon: ImageView
        val title: TextView
        val description: TextView
        val button: Button

        init {
            activity.setContentView(R.layout.activity_follow_up_notification_discovery)
            swipeIcon = activity.findViewById(R.id.discovery_screen_swipe_icon)
            title = activity.findViewById(R.id.discovery_screen_title)
            description = activity.findViewById(R.id.discovery_screen_description)
            button = activity.findViewById(R.id.discovery_screen_button)
        }

        override fun setScreenContent(content: ScreenContent) {
            swipeIcon.visibility = if (content.hasTopIcon) {
                View.VISIBLE
            } else {
                View.GONE
            }

            title.text = activity.getText(content.title)
            description.text = activity.getText(content.description)
        }

        override fun setOnButtonClick(onClick: () -> Unit) {
            button.setOnClickListener { onClick.invoke() }
        }
    }

    companion object {
        const val EXTRA_IS_REMINDER = "is_reminder"
    }
}
