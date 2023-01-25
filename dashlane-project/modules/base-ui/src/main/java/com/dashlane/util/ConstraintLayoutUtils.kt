package com.dashlane.util

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

inline fun ConstraintLayout.updateConstraints(operation: ConstraintSet.() -> Unit) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    operation.invoke(constraintSet)
    constraintSet.applyTo(this)
}