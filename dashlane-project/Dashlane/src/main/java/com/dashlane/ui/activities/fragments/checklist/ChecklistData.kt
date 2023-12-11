package com.dashlane.ui.activities.fragments.checklist

import android.graphics.Paint
import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.dashlane.databinding.ItemGetstartedBinding
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.COMPLETED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.COMPLETED_AND_ACKNOWLEDGED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.TO_COMPLETE
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.ADD_CREDENTIAL
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.AUTOFILL
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.DARK_WEB_MONITORING
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.M2D
import com.dashlane.ui.widgets.view.GetStartedStepView

data class ChecklistItem(
    val type: ChecklistData.ItemType,
    val state: ChecklistData.ItemState,
    @StringRes val actionLabelId: Int? = null
) {
    var onChecklistItemAcknowledge: (() -> Unit)? = null
}

data class ChecklistData(
    val addCredential: ChecklistItem?,
    val checkDarkWebAlerts: ChecklistItem?,
    val activatedAutofill: ChecklistItem,
    val m2d: ChecklistItem?,
    val isDismissible: Boolean,
    val hasSeenChecklist: Boolean
) {
    enum class ItemType { DARK_WEB_MONITORING, ADD_CREDENTIAL, AUTOFILL, M2D; }
    enum class ItemState { TO_COMPLETE, COMPLETED, COMPLETED_AND_ACKNOWLEDGED; }

    companion object {
        const val KEY_CHECKLIST_ADD_CREDENTIAL_ACKNOWLEDGED = "key_checklist_add_credential_acknowledged"
        const val KEY_CHECKLIST_AUTOFILL_ACTIVATION_ACKNOWLEDGED = "key_checklist_add_autofill_activation_acknowledged"
        const val KEY_CHECKLIST_M2D_ACKNOWLEDGED = "key_checklist_m2d_acknowledged"
        const val KEY_CHECKLIST_DWM_ACKNOWLEDGED = "key_checklist_dwm_acknowledged"
    }
}

object ChecklistViewSetup {
    fun setupView(
        getStartedBinding: ItemGetstartedBinding,
        item: ChecklistData?,
        onDismiss: () -> Unit,
        onImportPasswords: () -> Unit,
        onActivateAutofill: () -> Unit,
        onShowM2d: () -> Unit,
        onShowDarkWebMonitoring: () -> Unit
    ) {
        item ?: return

        val sceneRootId = getStartedBinding.root.id
        val step1 = getStep1(item, getStartedBinding)
        val step2 = getStartedBinding.step2
        val step3 = getStartedBinding.step3

        val stepsMap = buildMap {
            put((item.addCredential ?: item.checkDarkWebAlerts!!), step1)
            put(item.activatedAutofill, step2)
            item.m2d?.let { put(key = it, value = step3) }
        }

        
        if (item.m2d == null) {
            step3.visibility = View.GONE
        }
        val highlightNextStepUnit: () -> Unit = {
            getNextStepToHighlight(getStartedBinding, stepsMap)?.let { stepView ->
                stepView.setExpanded(value = true, animate = true, duration = stepView.completionDuration)
            }
        }

        stepsMap.forEach { (item, stepView) ->
            setupItem(
                stepView = stepView,
                checklistGroupId = sceneRootId,
                itemData = item,
                highlightNextStepUnit = highlightNextStepUnit,
                onImportPasswords = onImportPasswords,
                onActivateAutofill = onActivateAutofill,
                onShowM2d = onShowM2d,
                onShowDarkWebMonitoring = onShowDarkWebMonitoring
            )
        }

        setupDismissButton(getStartedBinding.dismiss, item, onDismiss)

        val isHighlightStepShipped = item.addCredential?.state == COMPLETED ||
            item.activatedAutofill.state == COMPLETED ||
            item.m2d?.state == COMPLETED ||
            item.checkDarkWebAlerts?.state == COMPLETED

        getStartedBinding.root.visibility = View.VISIBLE
        if (!isHighlightStepShipped) {
            val res = getStartedBinding.root.resources
            val screenLoadingBuffer = res.getInteger(android.R.integer.config_longAnimTime).toLong()
            val checklistAcknowledgedBuffer = res.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            val delay = when (item.hasSeenChecklist) {
                true -> screenLoadingBuffer
                false -> screenLoadingBuffer + checklistAcknowledgedBuffer
            }
            getStartedBinding.root.postDelayed({ highlightNextStepUnit.invoke() }, delay)
        }
    }

    private fun getStep1(
        item: ChecklistData,
        getStartedBinding: ItemGetstartedBinding
    ) = if (item.addCredential != null) {
        getStartedBinding.step1.apply {
            getStartedBinding.step1Dwm.visibility = View.GONE
        }
    } else {
        getStartedBinding.step1Dwm.apply {
            getStartedBinding.step1.visibility = View.GONE
            visibility = View.VISIBLE
        }
    }

    @SuppressWarnings("SpreadOperator")
    private fun getNextStepToHighlight(
        getStartedBinding: ItemGetstartedBinding,
        steps: Map<ChecklistItem, GetStartedStepView>
    ): GetStartedStepView? {
        val nextStep = steps.keys.firstOrNull { it.state == TO_COMPLETE }
        val completedStepIds =
            steps.filter { (item, _) -> item.state == COMPLETED }.map { (_, stepView) -> stepView }.toTypedArray()
        val overridableIds = listOf(View.NO_ID, *completedStepIds)
        return if (nextStep != null && overridableIds.contains(getStartedBinding.root.expandedId)) steps[nextStep] else null
    }

    private fun setupDismissButton(
        dismissButton: Button,
        item: ChecklistData,
        onDismiss: () -> Unit
    ) {
        dismissButton.paintFlags = dismissButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        dismissButton.setOnClickListener {
            onDismiss()
            
            dismissButton.setOnClickListener(null)
        }
        if (item.isDismissible) {
            dismissButton.visibility = View.VISIBLE
        } else {
            dismissButton.visibility = View.GONE
        }
    }

    private fun setupItem(
        stepView: GetStartedStepView,
        @IdRes checklistGroupId: Int,
        itemData: ChecklistItem,
        highlightNextStepUnit: () -> Unit,
        onImportPasswords: () -> Unit,
        onActivateAutofill: () -> Unit,
        onShowM2d: () -> Unit,
        onShowDarkWebMonitoring: () -> Unit
    ) {
        val ctaClickListener = View.OnClickListener {
            onChecklistItemCtaClick(
                type = itemData.type,
                importPasswords = onImportPasswords,
                activateAutofill = onActivateAutofill,
                showM2d = onShowM2d,
                showDarkWebMonitoring = onShowDarkWebMonitoring
            )
        }
        stepView.setCtaOnClickListener(ctaClickListener)
        stepView.sceneRootIdRes = checklistGroupId
        itemData.actionLabelId?.let { stepView.title.setText(it) }
        when (itemData.state) {
            TO_COMPLETE -> setToCompleteState(stepView)
            COMPLETED -> setCompleteState(stepView, itemData.onChecklistItemAcknowledge, highlightNextStepUnit)
            COMPLETED_AND_ACKNOWLEDGED -> setCompletedAndAcknowledgedState(stepView)
        }
    }

    private fun setToCompleteState(stepView: GetStartedStepView) {
        stepView.visibility = View.VISIBLE
        stepView.completion = false
        
    }

    private fun setCompleteState(
        stepView: GetStartedStepView,
        acknowledgeListener: (() -> Unit)?,
        highlightNextStepUnit: (() -> Unit)?
    ) {
        stepView.visibility = View.VISIBLE
        stepView.animateCompletion(highlightNextStepUnit, acknowledgeListener)
    }

    private fun setCompletedAndAcknowledgedState(stepView: GetStartedStepView) {
        stepView.visibility = View.VISIBLE
        stepView.completion = true
        stepView.setExpanded(value = false, animate = false)
    }

    private fun onChecklistItemCtaClick(
        type: ChecklistData.ItemType,
        importPasswords: () -> Unit,
        activateAutofill: () -> Unit,
        showM2d: () -> Unit,
        showDarkWebMonitoring: () -> Unit,
    ) {
        when (type) {
            ADD_CREDENTIAL -> {
                importPasswords()
            }

            AUTOFILL -> {
                activateAutofill()
            }

            M2D -> {
                showM2d()
            }

            DARK_WEB_MONITORING -> {
                showDarkWebMonitoring()
            }
        }
    }
}