package com.dashlane.item.v3.util

import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import kotlinx.coroutines.flow.update

@Suppress("UNCHECKED_CAST")
internal fun <T : FormData> ItemEditState<T>.revealPassword(
    sensitiveFieldLoader: SensitiveFieldLoader,
    id: String,
    field: SensitiveField,
    revealedFields: Set<SensitiveField>
): ItemEditState<T> {
    val data = datas?.current?.formData as? CredentialFormData ?: return this
    
    val password = data.password ?: CredentialFormData.Password(
        sensitiveFieldLoader.getSensitiveField(
            id,
            field
        )
    )
    return copy(
        datas = datas.copy(
            current = datas.current.copy(formData = data.copy(password = password) as T),
            initial = if ((datas.initial.formData as CredentialFormData).password == null) {
                datas.initial.copy(
                    formData = datas.initial.formData.copy(
                        password = password
                    ) as T
                )
            } else {
                datas.initial
            }
        ),
        revealedFields = revealedFields,
    )
}

internal fun <T : FormData> MutableViewStateFlow<ItemEditState<T>, out State.SideEffect>.updateMenuActions(
    menuActions: (ItemEditState<T>, Boolean) -> List<MenuAction>
) = update { state ->
    state.copy(
        menuActions = menuActions(state, state.isEditMode)
    )
}

internal fun <T : FormData> MutableViewStateFlow<ItemEditState<T>, out State.SideEffect>.updateCommonData(
    block: (CommonData) -> CommonData
) =
    update { state ->
        state.datas ?: return@update state 
        val commonData = block(state.datas.current.commonData)
        state.copy(
            datas = state.datas.copy(
                current = state.datas.current.copy(
                    commonData = commonData
                )
            )
        )
    }

internal fun <T : FormData> MutableViewStateFlow<ItemEditState<T>, out State.SideEffect>.updateFormData(block: (T) -> T) =
    update { state ->
        state.datas ?: return@update state 
        val formData = block(state.datas.current.formData)
        state.copy(
            datas = state.datas.copy(
                current = state.datas.current.copy(
                    formData = formData
                )
            )
        )
    }