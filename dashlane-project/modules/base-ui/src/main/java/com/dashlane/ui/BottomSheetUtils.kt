package com.dashlane.ui

import android.view.View
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import com.dashlane.util.DeviceUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

val BottomSheetDialogFragment.bottomSheetDialog get() = dialog as BottomSheetDialog?

val BottomSheetDialog.contentView get() = findViewById<FrameLayout>(R.id.design_bottom_sheet)!!



data class BottomSheetHeightConfig(
    @FloatRange(from = 0.0, to = 1.0) val peekHeightRatio: Float = 0.33f,
    @FloatRange(from = 0.0, to = 1.0) val expandedHeightRatio: Float = 0.9f
)



fun BottomSheetDialog.configureBottomSheet(
    config: BottomSheetHeightConfig? = null,
    behaviorState: Int = BottomSheetBehavior.STATE_EXPANDED,
    hideKeyboardWhenDragging: Boolean = true
) = if (isShowing) {
    applyConfiguration(config, behaviorState, hideKeyboardWhenDragging)
} else {
    setOnShowListener {
        applyConfiguration(config, behaviorState, hideKeyboardWhenDragging)
    }
}

private fun BottomSheetDialog.applyConfiguration(
    config: BottomSheetHeightConfig?,
    behaviorState: Int,
    hideKeyboardWhenDragging: Boolean
) {
    if (config != null) {
        setPeekHeightRatio(config.peekHeightRatio)
        setExpandedHeightRatio(config.expandedHeightRatio)
    }
    behavior.run {
        state = behaviorState

        removeBottomSheetCallback(HideKeyboardWhenDraggingBottomSheet)

        if (hideKeyboardWhenDragging) {
            addBottomSheetCallback(HideKeyboardWhenDraggingBottomSheet)
        }
    }
}

private fun BottomSheetDialog.setPeekHeightRatio(ratio: Float) {
    behavior.peekHeight = heightRatio(ratio)
}

private fun BottomSheetDialog.setExpandedHeightRatio(ratio: Float) {
    val content = contentView

    content.layoutParams = content.layoutParams.apply {
        height = heightRatio(ratio)
    }
}

private object HideKeyboardWhenDraggingBottomSheet : BottomSheetBehavior.BottomSheetCallback() {
    override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
            DeviceUtils.hideKeyboard(bottomSheet)
        }
    }
}

private fun BottomSheetDialog.heightRatio(ratio: Float) =
    (ratio * contentView.resources.displayMetrics.heightPixels).toInt()
