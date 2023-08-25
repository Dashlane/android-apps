package com.dashlane.guidedonboarding.darkwebmonitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.guidedonboarding.R

class OnboardingDarkWebMonitoringLoadingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_dark_web_monitoring_loading, container, false)
    }
}