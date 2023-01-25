package com.dashlane.plans.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.R

class PurchaseCheckingErrorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_purchase_checking_result, container, false).also {
            PurchaseCheckingResultViewProxy(it).setError { activity?.finish() }
        }
    }
}