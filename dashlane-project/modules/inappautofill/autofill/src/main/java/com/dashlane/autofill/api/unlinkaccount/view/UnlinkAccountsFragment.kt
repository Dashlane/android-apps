package com.dashlane.autofill.api.unlinkaccount.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsComponent
import com.dashlane.autofill.api.unlinkaccount.dagger.DaggerUnlinkAccountsFragmentComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.getParcelableCompat
import javax.inject.Inject

class UnlinkAccountsFragment : Fragment() {

    @Inject
    lateinit var viewProxy: UnlinkAccountsViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivityComponent()
    }

    private fun initActivityComponent() {
        val fragmentActivity = this.activity ?: return
        val autoFillFormSource = arguments?.getParcelableCompat<AutoFillFormSource>(FORM_SOURCE) ?: return

        val viewModel = ViewModelProvider(
            fragmentActivity,
            UnlinkAccountsViewModel.Factory(fragmentActivity.application, autoFillFormSource)
        ).get(UnlinkAccountsViewModel::class.java)

        val activityComponent = DaggerUnlinkAccountsFragmentComponent.factory()
            .create(
                AutofillApiComponent(fragmentActivity.application),
                AutofillApiUnlinkAccountsComponent(fragmentActivity.application),
                viewModel.component,
                this
            )
        activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return viewProxy.setContentView(inflater, container)
    }

    override fun onResume() {
        super.onResume()
        viewProxy.onResume()
    }

    companion object {

        private const val FORM_SOURCE = "FORM_SOURCE"

        fun newInstance(autoFillFormSource: AutoFillFormSource): UnlinkAccountsFragment {
            val fragment = UnlinkAccountsFragment()
            fragment.arguments = Bundle().apply { putParcelable(FORM_SOURCE, autoFillFormSource) }
            return fragment
        }
    }
}
