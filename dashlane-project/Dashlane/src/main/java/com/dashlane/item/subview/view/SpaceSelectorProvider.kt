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
import com.dashlane.teamspaces.adapter.TeamspaceSpinnerAdapter
import com.dashlane.teamspaces.adapter.SpinnerUtil
import com.dashlane.ui.widgets.Notificator
import com.dashlane.util.Toaster

object SpaceSelectorProvider {

    fun create(
        activity: FragmentActivity,
        current: Teamspace,
        values: List<Teamspace>,
        editable: Boolean = false,
        item: ItemEditSpaceSubView?,
        toaster: Toaster,
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
            updateEnableState(
                activity = activity,
                view = view,
                enable = item.changeEnable,
                toaster = toaster
            )
            item.enableValueChangeManager.addValueChangedListener(object : ValueChangeManager.Listener<Boolean> {
                override fun onValueChanged(origin: Any, newValue: Boolean) {
                    updateEnableState(
                        activity = activity,
                        view = view,
                        enable = newValue,
                        toaster = toaster
                    )
                }
            })
        }

        return view
    }

    private fun updateEnableState(activity: FragmentActivity, view: LinearLayout, enable: Boolean, toaster: Toaster) {
        foundSpinner(view)?.let {
            setEnable(
                activity = activity,
                spinner = it,
                editable = enable,
                toaster = toaster
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEnable(activity: FragmentActivity, spinner: Spinner, editable: Boolean, toaster: Toaster) {
        val adapter = spinner.adapter as TeamspaceSpinnerAdapter

        if (editable) {
            SpinnerUtil.enableSpinner(spinner)
            adapter.isDisabled = false
        } else {
            SpinnerUtil.disableSpinner(spinner)
            spinner.isEnabled = true
            spinner.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    showDisabledTeamspaceNotification(
                        activity = activity,
                        teamspace = adapter.getItem(spinner.selectedItemPosition)!!,
                        toaster = toaster
                    )
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

    private fun showDisabledTeamspaceNotification(activity: FragmentActivity, teamspace: Teamspace, toaster: Toaster) {
        val message = activity.getString(
            R.string.teamspace_forced_categorisation_restricted_domain_with_spacename,
            teamspace.teamName
        )
        Notificator(toaster).customErrorDialogMessage(
            activity = activity,
            topic = null,
            message = message,
            shouldCloseCaller = false
        )
    }
}