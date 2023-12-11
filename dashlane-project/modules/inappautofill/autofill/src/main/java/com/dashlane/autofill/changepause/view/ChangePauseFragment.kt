package com.dashlane.autofill.changepause.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.autofill.changepause.ChangePauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.Toaster
import com.dashlane.util.getParcelableCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePauseFragment : Fragment() {

    private lateinit var viewProxy: ChangePauseViewProxy

    @Inject
    lateinit var presenter: ChangePauseContract.Presenter

    @Inject
    lateinit var changePauseViewTypeProviderFactory: ChangePauseViewTypeProviderFactory

    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val autoFillFormSource =
            arguments?.getParcelableCompat<AutoFillFormSource>(FORM_SOURCE) ?: return
        viewProxy = ChangePauseViewProxy(
            this,
            presenter,
            changePauseViewTypeProviderFactory,
            toaster,
            autoFillFormSource
        )
        presenter.setView(viewProxy)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
