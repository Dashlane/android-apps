package com.dashlane.notificationcenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.notificationcenter.NotificationCenterLogger.OriginProvider
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationCenterFragment : AbstractContentFragment(), OriginProvider {

    @Inject
    lateinit var dataProvider: NotificationCenterDataProvider

    @Inject
    lateinit var logger: NotificationCenterLogger

    @Inject
    lateinit var presenter: NotificationCenterPresenter

    override val origin: String
        get() = NotificationCenterFragmentArgs.fromBundle(requireArguments()).origin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setCurrentPageView(AnyPage.NOTIFICATION_HOME)
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(false)
        val view = inflater.inflate(R.layout.fragment_actionitem_center, container, false)
        presenter.setProvider(dataProvider)
        presenter.setView(createViewProxy(view))
        if (savedInstanceState == null) {
            logger.logActionItemCenterShow()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        if (!requireActivity().isChangingConfigurations) {
            presenter.markAsRead()
        }
    }

    private fun createViewProxy(view: View): NotificationCenterViewProxy =
        NotificationCenterViewProxy(view, true)
}