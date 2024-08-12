package com.dashlane.autofill.frozenautofill

import com.dashlane.ui.model.TextResource

data class FrozenAutofillState(
    val description: TextResource,
    val bottomSheetState: BottomSheetState
) {

    enum class BottomSheetState {
        FOLDED,
        EXPANDED
    }
}