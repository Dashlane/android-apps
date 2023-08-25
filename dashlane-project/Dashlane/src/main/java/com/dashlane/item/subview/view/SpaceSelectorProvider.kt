package com.dashlane.item.subview.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.FragmentActivity
import com.dashlane.R
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.adapters.TeamspaceSpinnerAdapter
import com.dashlane.ui.util.SpinnerUtil
import com.dashlane.ui.widgets.Notificator

object SpaceSelectorProvider {

    fun create(
        activity: FragmentActivity,
        current: Teamspace,
        values: List<Teamspace>,
        editable: Boolean = false,
        item: ItemEditSpaceSubView?,
        selectionAction: ((Int) -> Unit)? = null
    ): LinearLayout {
        val view = SpinnerInputProvider.create(
            activity,
            title = activity.getString(R.string.teamspaces_selector_label),
            defaultValue = current,
            editable = editable,
            promptText = null,
            adapter = TeamspaceSpinnerAdapter(activity, values),
            currentValueChangeManager = item,
            selectionAction = selectionAction
        )

        if (editable && item != null) {
            updateEnableState(activity, view, item.changeEnable)
            item.enableValueChangeManager.addValueChangedListener(object : ValueChangeManager.Listener<Boolean> {
                override fun onValueChanged(origin: Any, newValue: Boolean) {
                    updateEnableState(activity, view, newValue)
                }
            })
        }

        return view
    }

    private fun updateEnableState(activity: FragmentActivity, view: LinearLayout, enable: Boolean) {
        foundSpinner(view)?.let { setEnable(it, enable, activity) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEnable(it: Spinner, editable: Boolean, activity: FragmentActivity) {
        val adapter = it.adapter as TeamspaceSpinnerAdapter

        if (editable) {
            SpinnerUtil.enableSpinner(it)
            adapter.isDisabled = false
        } else {
            SpinnerUtil.disableSpinner(it)
            it.isEnabled = true
            it.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    showDisabledTeamspaceNotification(activity, adapter.getItem(it.selectedItemPosition)!!)
                }
                true
            }
            adapter.isDisabled = true
        }
    }

    private fun foundSpinner(rootView: View): Spinner? {
        if (rootView is Spinner) {
            return rootView
        } else if (rootView is ViewGroup) {
            (0 until rootView.childCount).forEach {
                foundSpinner(rootView.getChildAt(it))?.let { return it }
            }
        }
        return null
    }

    private fun showDisabledTeamspaceNotification(activity: FragmentActivity, teamspace: Teamspace) {
        val message = activity.getString(
            R.string.teamspace_forced_categorisation_restricted_domain_with_spacename,
            teamspace.teamName
        )
        Notificator.customErrorDialogMessage(activity, null, message, false)
    }
}