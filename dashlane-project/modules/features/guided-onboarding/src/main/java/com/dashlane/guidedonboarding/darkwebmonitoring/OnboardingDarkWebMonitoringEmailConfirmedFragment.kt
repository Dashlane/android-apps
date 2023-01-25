package com.dashlane.guidedonboarding.darkwebmonitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dashlane.guidedonboarding.R



class OnboardingDarkWebMonitoringEmailConfirmedFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_dark_web_monitoring_email_confirmed, container, false)
            .also {
                it.findViewById<Button>(R.id.onboarding_dwm_close_button).setOnClickListener { activity?.finish() }
            }
    }
}