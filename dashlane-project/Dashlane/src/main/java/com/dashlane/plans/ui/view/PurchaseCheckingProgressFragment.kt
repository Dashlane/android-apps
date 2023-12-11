package com.dashlane.plans.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.dashlane.R

class PurchaseCheckingProgressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_purchase_checking_loading, container, false)

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val defaultAnimation = super.onCreateAnimation(transit, enter, nextAnim)
        return when (enter) {
            true -> {
                val slideInAnimation = context?.let { AnimationUtils.loadAnimation(it, R.anim.slide_in_bottom) }
                defaultAnimation?.let {
                    slideInAnimation?.duration = it.duration
                    slideInAnimation?.interpolator = it.interpolator
                }
                slideInAnimation
            }
            false -> defaultAnimation
        }
    }
}