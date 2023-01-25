package com.dashlane.autofill.api.changepause.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseComponent
import com.dashlane.autofill.api.changepause.dagger.DaggerChangePauseFragmentComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.getParcelableCompat
import javax.inject.Inject



class ChangePauseFragment : Fragment() {

    @Inject
    lateinit var viewProxy: ChangePauseViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivityComponent()
    }

    private fun initActivityComponent() {
        val fragmentActivity = this.activity ?: return
        val autoFillFormSource = arguments?.getParcelableCompat<AutoFillFormSource>(FORM_SOURCE) ?: return

        val viewModel = ViewModelProvider(
            fragmentActivity,
            ChangePauseViewModel.Factory(fragmentActivity.application, autoFillFormSource)
        ).get(ChangePauseViewModel::class.java)

        val activityComponent = DaggerChangePauseFragmentComponent.factory()
            .create(
                AutofillApiComponent(fragmentActivity.application),
                AutofillApiChangePauseComponent(fragmentActivity.application),
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

        fun newInstance(autoFillFormSource: AutoFillFormSource): ChangePauseFragment {
            val fragment = ChangePauseFragment()
            fragment.arguments = Bundle().apply { putParcelable(FORM_SOURCE, autoFillFormSource) }
            return fragment
        }
    }
}
