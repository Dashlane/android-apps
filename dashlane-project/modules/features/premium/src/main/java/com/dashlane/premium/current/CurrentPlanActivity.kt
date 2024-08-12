package com.dashlane.premium.current

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dashlane.navigation.Navigator
import com.dashlane.premium.current.model.CurrentPlan
import com.dashlane.premium.current.ui.CurrentBenefitItem
import com.dashlane.premium.current.ui.DarkWebMonitoringBottomSheetDialogFragment
import com.dashlane.premium.databinding.ActivityCurrentPlanBinding
import com.dashlane.premium.offer.common.model.OfferType
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CurrentPlanActivity : DashlaneActivity() {

    @Inject
    lateinit var currentPlanLogger: CurrentPlanLogger

    @Inject
    lateinit var navigator: Navigator

    private val viewModel: CurrentPlanViewModel by viewModels()
    private val benefitsAdapter = DashlaneRecyclerAdapter<CurrentBenefitItem>()

    private lateinit var binding: ActivityCurrentPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.currentPlanBenefitsContainer.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = benefitsAdapter
        }

        benefitsAdapter.setOnItemClickListener { _, _, item, _ ->
            item?.let { viewModel.onBenefitItemClicked(it) }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    CurrentPlanState.Init,
                    CurrentPlanState.Loading -> Unit
                    is CurrentPlanState.Loaded -> {
                        showCurrentPlan(uiState.uiData.plan)
                    }
                    is CurrentPlanState.NavigateToPlansPage -> navigateToPlanPage(uiState.offerType)
                    is CurrentPlanState.CloseWithoutAction -> {
                        viewModel.hasNavigated()
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.dwmBottomSheetState.collect { dwmState ->
                when (dwmState) {
                    DarkWebMonitoringInfoState.Displaying -> displayDarkWebMonitoringBottomSheet()
                    DarkWebMonitoringInfoState.Initial -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun navigateToPlanPage(offerType: OfferType?) {
        viewModel.hasNavigated()
        navigator.goToOffers(offerType = offerType?.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.onDestroy()
        }
    }

    private fun showCurrentPlan(currentPlan: CurrentPlan?) {
        currentPlan ?: return
        binding.currentPlanTitle.text = currentPlan.title.format(resources)
        val items = currentPlan.benefits.map { CurrentBenefitItem(it) }
        benefitsAdapter.populateItems(items)
        setSuggestionContent(currentPlan.suggestion)
        val recommendedActions =
            listOfNotNull(currentPlan.primaryAction.type, currentPlan.secondaryAction?.type)
        setCta(
            button = binding.currentPlanPrimaryCta,
            action = currentPlan.primaryAction,
            recommendedActions = recommendedActions
        )
        setCta(
            button = binding.currentPlanSecondaryCta,
            action = currentPlan.secondaryAction,
            recommendedActions = recommendedActions
        )
    }

    private fun setSuggestionContent(suggestion: CurrentPlan.Suggestion?) {
        binding.currentPlanSuggestion.apply {
            if (suggestion == null) {
                isVisible = false
            } else {
                title = suggestion.title?.format(resources)
                text = suggestion.text.format(resources)
                isVisible = true
            }
        }
    }

    private fun setCta(
        button: Button,
        action: CurrentPlan.Action?,
        recommendedActions: List<CurrentPlan.Action.Type>
    ) =
        if (action == null) {
            button.isVisible = false
        } else {
            button.setText(action.label)
            if (!button.hasOnClickListeners()) {
                button.setOnClickListener {
                    viewModel.onActionClicked(
                        action.type,
                        recommendedActions
                    )
                }
            }
            button.isVisible = true
        }

    private fun displayDarkWebMonitoringBottomSheet() {
        var dialog =
            supportFragmentManager.findFragmentByTag(DarkWebMonitoringBottomSheetDialogFragment.DIALOG_TAG)
        if (dialog == null) {
            dialog = DarkWebMonitoringBottomSheetDialogFragment.newInstance()
            dialog.show(
                supportFragmentManager,
                DarkWebMonitoringBottomSheetDialogFragment.DIALOG_TAG
            )
            viewModel.onDarkWebMonitoringInfoShown()
        }
    }
}