package com.dashlane.autofill.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingInAppLoginFragmentStatePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private var onboardingInAppLoginFragments = arrayOf<Fragment>()

    fun setOnboardingInAppLoginFragments(onboardingInAppLoginFragments: Array<Fragment>) {
        this.onboardingInAppLoginFragments = onboardingInAppLoginFragments
    }

    override fun getItemCount(): Int {
        return onboardingInAppLoginFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return onboardingInAppLoginFragments[position]
    }
}