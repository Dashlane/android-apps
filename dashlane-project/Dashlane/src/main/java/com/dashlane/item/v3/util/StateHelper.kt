package com.dashlane.item.v3.util

import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal fun State.revealPassword(
    sensitiveFieldLoader: SensitiveFieldLoader,
    id: String,
    field: SensitiveField,
    revealedFields: Set<SensitiveField>
): State {
    val data = (formData as? CredentialFormData) ?: return this
    
    val password = data.password ?: CredentialFormData.Password(
        sensitiveFieldLoader.getSensitiveField(
            id,
            field
        )
    )
    val newData = data.copy(password = password)
    return copy(
        formData = newData,
        revealedFields = revealedFields,
    )
}

internal fun MutableStateFlow<State>.updateMenuActions(
    menuActions: (State, Boolean) -> List<MenuAction>
) = update { state ->
    state.copy(
        menuActions = menuActions(state, state.isEditMode)
    )
}

internal fun MutableStateFlow<State>.updateFormData(block: (FormData) -> FormData) =
    update { state ->
        val formData = block(state.formData)
        state.copy(formData = formData)
    }

internal fun MutableStateFlow<State>.updateCredentialFormData(
    block: (CredentialFormData) -> CredentialFormData
) = updateFormData {
    if (it is CredentialFormData) {
        block(it)
    } else {
        it
    }
}