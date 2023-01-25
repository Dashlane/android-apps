package com.dashlane.notificationcenter.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.notificationcenter.NotificationCenterDataProvider
import com.dashlane.notificationcenter.NotificationCenterLogger
import com.dashlane.notificationcenter.NotificationCenterPresenter
import com.dashlane.notificationcenter.NotificationCenterViewProxy
import com.dashlane.notificationcenter.view.ActionItemSection
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationCenterSectionDetailsFragment : Fragment(), NotificationCenterLogger.OriginProvider {
    @Inject
    lateinit var dataProvider: NotificationCenterDataProvider

    @Inject
    lateinit var presenter: NotificationCenterPresenter

    override val origin: String
        get() = NotificationCenterLogger.ORIGIN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_notification_center_details, null)
        val section =
            ActionItemSection.valueOf(NotificationCenterSectionDetailsFragmentArgs.fromBundle(requireArguments()).extraSection)
        section.toPage()?.let { setCurrentPageView(it) }
        presenter.section = section
        presenter.setProvider(dataProvider)
        presenter.setView(NotificationCenterViewProxy(view.findViewById(R.id.root), false))
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.run {
            if (!isChangingConfigurations) {
                presenter.markAsRead()
            }
        }
    }
}

private fun ActionItemSection.toPage(): AnyPage? = when (this) {
    ActionItemSection.BREACH_ALERT -> AnyPage.NOTIFICATION_SECURITY_LIST
    ActionItemSection.GETTING_STARTED -> AnyPage.NOTIFICATION_GETTING_STARTED_LIST
    ActionItemSection.WHATS_NEW -> AnyPage.NOTIFICATION_NEW_LIST
    ActionItemSection.SHARING -> AnyPage.NOTIFICATION_SHARING_LIST
    ActionItemSection.YOUR_ACCOUNT -> AnyPage.NOTIFICATION_YOUR_ACCOUNT_LIST
    ActionItemSection.PROMOTIONS -> AnyPage.NOTIFICATION_PROMOTIONS_LIST
}