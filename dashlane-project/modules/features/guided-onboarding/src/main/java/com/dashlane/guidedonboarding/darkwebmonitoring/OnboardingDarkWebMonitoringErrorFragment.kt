package com.dashlane.guidedonboarding.darkwebmonitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dashlane.guidedonboarding.R

class OnboardingDarkWebMonitoringErrorFragment : Fragment() {
    interface Listener {
        fun onTryAgain()
        fun onSkip()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_dark_web_monitoring_error, container, false)
            .also {
                it.findViewById<Button>(R.id.onboarding_dwm_skip_button).setOnClickListener {
                    (activity as? Listener)?.onSkip()
                }
                it.findViewById<Button>(R.id.onboarding_dwm_try_again_button).setOnClickListener {
                    (activity as? Listener)?.onTryAgain()
                }
            }
    }
}