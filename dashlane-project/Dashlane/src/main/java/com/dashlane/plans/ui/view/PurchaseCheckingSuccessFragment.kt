package com.dashlane.plans.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.R

class PurchaseCheckingSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_purchase_checking_result, container, false)

        val args = PurchaseCheckingSuccessFragmentArgs.fromBundle(requireArguments())
        PurchaseCheckingResultViewProxy(rootView).setSuccess(
            planName = args.planName,
            familyBundle = args.familyBundle
        ) {
            activity?.finish()
        }

        return rootView
    }
}