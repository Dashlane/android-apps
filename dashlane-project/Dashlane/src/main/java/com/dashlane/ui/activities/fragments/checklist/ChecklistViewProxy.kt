package com.dashlane.ui.activities.fragments.checklist

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.dashlane.R
import com.dashlane.databinding.FragmentChecklistBinding
import com.dashlane.navigation.Navigator
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.activities.fragments.checklist.ChecklistViewSetup.setupView
import kotlinx.coroutines.launch

class ChecklistViewProxy(
    private val viewModel: ChecklistViewModelContract,
    private val binding: FragmentChecklistBinding,
    private val lifecycle: Lifecycle,
    private val navigator: Navigator,
    private val m2xIntentFactory: M2xIntentFactory
) {
    private val context
        get() = binding.root.context

    init {
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.checkListDataFlow.collect {
                    setChecklist(it)
                }
            }
        }
    }

    private fun setChecklist(checklistData: ChecklistData?) {
        setupView(
            getStartedBinding = binding.checklistGroup,
            item = checklistData,
            onDismiss = ::onDismiss,
            onImportPasswords = { navigator.goToCredentialAddStep1(expandImportOptions = true) },
            onActivateAutofill = { navigator.goToInAppLoginIntro() },
            onShowM2d = {
                val m2xIntent = m2xIntentFactory.buildM2xConnect()
                context.startActivity(m2xIntent)
            },
            onShowDarkWebMonitoring = { navigator.goToDarkWebMonitoring() }
        )
    }

    private fun onDismiss() {
        viewModel.onDismissChecklistClicked()
        playDismissAnimation()
    }

    private fun playDismissAnimation() {
        val windowManager = context.getSystemService<WindowManager>() ?: return
        val layout = LayoutInflater.from(context).inflate(R.layout.window_lottie_confetti, null)
        val lottieAnimationView =
            layout.findViewById<LottieAnimationView>(R.id.lottie_checklist_completion_view)

        val params = WindowManager.LayoutParams()
        params.gravity = Gravity.CENTER
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
        params.format = PixelFormat.TRANSLUCENT
        windowManager.addView(layout, params)

        lottieAnimationView.addAnimatorListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    windowManager.removeView(layout)
                }
            }
        )
        lottieAnimationView.playAnimation()
    }
}