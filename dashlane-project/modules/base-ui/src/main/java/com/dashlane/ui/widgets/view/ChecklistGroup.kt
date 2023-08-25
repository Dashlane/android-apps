package com.dashlane.ui.widgets.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.parcelize.Parcelize

class ChecklistGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @IdRes
    var expandedId: Int = View.NO_ID
        private set(@IdRes value) {
            if (value != field) {
                setCollapsedStateForView(field)
                field = value
            }
        }

    private var protectionFlag = false
    private var childOnCheckedChangeListener = object : OnExpandChangeListener {
        override fun onExpandedChange(view: GetStartedStepView, expanded: Boolean) {
            
            
            if (!protectionFlag) {
                protectionFlag = true
                expandedId = when (expanded) {
                    true -> view.id
                    
                    false -> View.NO_ID
                }
                protectionFlag = false
            }
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child is GetStartedStepView) {
            child.enforceId()
            child.onExpandedChangeListener = childOnCheckedChangeListener

            if (child.expanded) {
                expandedId = child.id
            }
        }
        super.addView(child, index, params)
    }

    override fun removeView(view: View?) {
        (view as? GetStartedStepView)?.onExpandedChangeListener = null
        super.removeView(view)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SaveState(
            superState = super.onSaveInstanceState(),
            expandedId = expandedId
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SaveState)?.let { saveState ->
            super.onRestoreInstanceState(saveState.superState)
            expandedId = saveState.expandedId
        }
    }

    private fun setCollapsedStateForView(viewId: Int) {
        if (viewId != View.NO_ID) {
            (findViewById<View>(viewId) as? GetStartedStepView)?.setExpanded(value = false, animate = true)
        }
    }

    private fun View.enforceId() {
        if (id == View.NO_ID) {
            id = View.generateViewId()
        }
    }

    interface OnExpandChangeListener {
        fun onExpandedChange(view: GetStartedStepView, expanded: Boolean)
    }

    @Parcelize
    private data class SaveState(
        val superState: Parcelable?,
        @IdRes val expandedId: Int
    ) : Parcelable
}