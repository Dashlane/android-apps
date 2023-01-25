package com.dashlane.premium.current

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.premium.R
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.current.ui.CurrentBenefitItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.Infobox
import com.dashlane.util.TextViewText
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class CurrentPlanViewProxy(activity: AppCompatActivity) :
    BaseViewProxy<CurrentPlanContract.Presenter>(activity), CurrentPlanContract.ViewProxy {

    private var title by TextViewText(activity.findViewById(R.id.current_plan_title) as TextView)
    private val benefitsAdapter = DashlaneRecyclerAdapter<CurrentBenefitItem>()
    private val infobox = activity.findViewById(R.id.current_plan_suggestion) as Infobox
    private val primaryCta = activity.findViewById(R.id.current_plan_primary_cta) as Button
    private val secondaryCta = activity.findViewById(R.id.current_plan_secondary_cta) as Button

    init {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.current_plan_benefits_container)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = benefitsAdapter
        }
        benefitsAdapter.setOnItemClickListener { _, _, item, _ ->
            item?.let { presenterOrNull?.onItemClicked(it) }
        }
    }

    override fun showCurrentPlan(currentPlan: CurrentPlan) {
        title = currentPlan.title.format(resources)
        val items = currentPlan.benefits.map { CurrentBenefitItem(it) }
        benefitsAdapter.populateItems(items)
        setSuggestionContent(currentPlan.suggestion)
        setCta(primaryCta, currentPlan.primaryAction)
        setCta(secondaryCta, currentPlan.secondaryAction)
    }

    private fun setCta(button: Button, action: CurrentPlan.Action?) =
        if (action == null) {
            button.isVisible = false
        } else {
            button.setText(action.label)
            button.setOnClickListener { presenterOrNull?.onActionClicked(action.type) }
            button.isVisible = true
        }

    private fun setSuggestionContent(suggestion: CurrentPlan.Suggestion?) =
        if (suggestion == null) {
            infobox.isVisible = false
        } else {
            infobox.title = suggestion.title?.format(resources)
            infobox.text = suggestion.text.format(resources)
            infobox.isVisible = true
        }
}