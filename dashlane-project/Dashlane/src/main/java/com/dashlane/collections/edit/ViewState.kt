package com.dashlane.collections.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.teamspaces.model.TeamSpace

sealed class ViewState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : ViewState()
    data class Form(override val viewData: ViewData) : ViewState()
    data class Saving(override val viewData: ViewData) : ViewState()
    data class Error(
        override val viewData: ViewData,
        val fieldError: FieldError? = null,
        val snackError: SnackError? = null
    ) : ViewState()
    data class Saved(override val viewData: ViewData) : ViewState()
}

data class ViewData(
    val collectionName: TextFieldValue,
    val editMode: Boolean,
    val space: TeamSpace?,
    val availableSpaces: List<TeamSpace>?
)

enum class FieldError {
    COLLECTION_ALREADY_EXISTS,
    EMPTY_NAME
}

enum class SnackError {
    SHARED_COLLECTION_RENAME_GENERIC_ERROR
}
