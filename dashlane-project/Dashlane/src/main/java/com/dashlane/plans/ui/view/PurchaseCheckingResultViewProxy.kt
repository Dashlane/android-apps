package com.dashlane.plans.ui.view

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dashlane.R

class PurchaseCheckingResultViewProxy(rootView: View) {
    private val icon: ImageView = rootView.findViewById(R.id.plan_bought_thanks_icon)
    private val title: TextView = rootView.findViewById(R.id.plan_bought_thanks_title)
    private val description: TextView = rootView.findViewById(R.id.plan_bought_thanks_description)
    private val closeButton: Button = rootView.findViewById(R.id.plan_bought_close)

    fun setSuccess(planName: String, familyBundle: Boolean, onCloseAction: () -> Unit) {
        icon.setImageResource(R.drawable.ic_purchase_check_success)
        title.text = title.resources.getString(R.string.plan_bought_thanks_title, planName)
        description.setText(getDescriptionResId(familyBundle))
        closeButton.setOnClickListener { onCloseAction.invoke() }
    }

    private fun getDescriptionResId(familyBundle: Boolean) =
        R.string.family_plan_bought_thanks_message.takeIf { familyBundle }
            ?: R.string.plan_bought_thanks_message

    fun setError(onCloseAction: () -> Unit) {
        icon.setImageResource(R.drawable.ic_purchase_check_error)
        title.setText(R.string.plan_check_purchase_error_title)
        description.setText(R.string.plan_check_purchase_error_message)
        closeButton.setOnClickListener { onCloseAction.invoke() }
    }
}