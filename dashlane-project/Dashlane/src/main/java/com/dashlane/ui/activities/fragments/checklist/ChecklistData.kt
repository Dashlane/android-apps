package com.dashlane.ui.activities.fragments.checklist

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.COMPLETED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.COMPLETED_AND_ACKNOWLEDGED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState.TO_COMPLETE
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.ADD_CREDENTIAL
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.AUTOFILL
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.DARK_WEB_MONITORING
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemType.M2D
import com.dashlane.ui.widgets.view.ChecklistGroup
import com.dashlane.ui.widgets.view.GetStartedStepView
import com.dashlane.useractivity.log.usage.UsageLogCode94

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
    val m2d: ChecklistItem,
    val isDismissable: Boolean,
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
        itemView: ChecklistGroup,
        item: ChecklistData?,
        onDismiss: () -> Unit
    ) {
        item ?: return

        val logger = ChecklistLogger(
            SingletonProvider.getSessionManager(),
            SingletonProvider.getComponent().bySessionUsageLogRepository
        )

        logger.logDisplay(item.isDismissable, item.checkDarkWebAlerts != null)

        val sceneRootId = itemView.id
        val step1 = getStep1(item, itemView)
        val step2 = itemView.findViewById<GetStartedStepView>(R.id.step2)
        val step3 = itemView.findViewById<GetStartedStepView>(R.id.step3)

        val stepsMap = mapOf(
            (item.addCredential ?: item.checkDarkWebAlerts!!) to step1,
            item.activatedAutofill to step2,
            item.m2d to step3
        )

        val highlightNextStepUnit: () -> Unit = {
            getNextStepToHighlight(itemView, stepsMap)?.let { stepView ->
                stepView.setExpanded(value = true, animate = true, duration = stepView.completionDuration)
            }
        }

        stepsMap.forEach { (item, stepView) ->
            setupItem(stepView, sceneRootId, item, logger, highlightNextStepUnit)
        }

        setupDismissButton(itemView, logger, item, onDismiss)

        val isHighlightStepShipped = item.addCredential?.state == COMPLETED ||
            item.activatedAutofill.state == COMPLETED ||
            item.m2d.state == COMPLETED ||
            item.checkDarkWebAlerts?.state == COMPLETED

        itemView.visibility = View.VISIBLE
        if (!isHighlightStepShipped) {
            val res = itemView.resources
            val screenLoadingBuffer = res.getInteger(android.R.integer.config_longAnimTime).toLong()
            val checklistAcknowledgedBuffer = res.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            val delay = when (item.hasSeenChecklist) {
                true -> screenLoadingBuffer
                false -> screenLoadingBuffer + checklistAcknowledgedBuffer
            }
            itemView.postDelayed({ highlightNextStepUnit.invoke() }, delay)
        }
    }

    private fun getStep1(
        item: ChecklistData,
        itemView: ChecklistGroup
    ) = if (item.addCredential != null) {
        itemView.findViewById<GetStartedStepView>(R.id.step1).apply {
            itemView.findViewById<GetStartedStepView>(R.id.step1_dwm).visibility = View.GONE
        }
    } else {
        itemView.findViewById<GetStartedStepView>(R.id.step1_dwm).apply {
            itemView.findViewById<GetStartedStepView>(R.id.step1).visibility = View.GONE
            visibility = View.VISIBLE
        }
    }

    @SuppressWarnings("SpreadOperator")
    private fun getNextStepToHighlight(
        itemView: ChecklistGroup,
        steps: Map<ChecklistItem, GetStartedStepView>
    ): GetStartedStepView? {
        val nextStep = steps.keys.firstOrNull { it.state == TO_COMPLETE }
        val completedStepIds =
            steps.filter { (item, _) -> item.state == COMPLETED }.map { (_, stepView) -> stepView }.toTypedArray()
        val overridableIds = listOf(View.NO_ID, *completedStepIds)
        return if (nextStep != null && overridableIds.contains(itemView.expandedId)) steps[nextStep] else null
    }

    private fun setupDismissButton(
        itemView: ChecklistGroup,
        logger: ChecklistLogger,
        item: ChecklistData,
        onDismiss: () -> Unit
    ) {
        val dismissButton = itemView.findViewById<Button>(R.id.dismiss)
        dismissButton.paintFlags = dismissButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        dismissButton.setOnClickListener {
            logger.logClickDismiss()
            onDismiss()
            
            dismissButton.setOnClickListener(null)
        }
        if (item.isDismissable) {
            dismissButton.visibility = View.VISIBLE
        } else {
            dismissButton.visibility = View.GONE
        }
    }

    private fun setupItem(
        stepView: GetStartedStepView,
        @IdRes checklistGroupId: Int,
        itemData: ChecklistItem,
        logger: ChecklistLogger,
        highlightNextStepUnit: () -> Unit
    ) {
        val ctaClickListener = View.OnClickListener { onChecklistItemCtaClick(itemData.type, logger, stepView.context) }
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

    private fun onChecklistItemCtaClick(type: ChecklistData.ItemType, logger: ChecklistLogger, context: Context) {
        when (type) {
            ADD_CREDENTIAL -> {
                logger.logClickAddAccount()
                importPasswords()
            }
            AUTOFILL -> {
                logger.logClickActivateAutofill()
                activateAutofill()
            }
            M2D -> {
                logger.logClickAddComputer()
                showM2D(context)
            }
            DARK_WEB_MONITORING -> {
                logger.logClickDarkWebMonitoring()
                showDarkWebMonitoring()
            }
        }
    }

    private fun importPasswords() {
        SingletonProvider.getNavigator().goToCredentialAddStep1(sender = "get_started", expandImportOptions = true)
    }

    private fun activateAutofill() {
        SingletonProvider.getNavigator().goToInAppLoginIntro()
    }

    private fun showM2D(context: Context) {
        val m2xIntent = SingletonProvider.getComponent().m2xIntentFactory
            .buildM2xConnect(UsageLogCode94.Origin.DASHBOARD_GETTING_STARTED.code)
        context.startActivity(m2xIntent)
    }

    private fun showDarkWebMonitoring() {
        SingletonProvider.getNavigator().goToDarkWebMonitoring("get_started")
    }
}