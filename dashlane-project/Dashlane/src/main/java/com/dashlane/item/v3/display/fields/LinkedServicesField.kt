package com.dashlane.item.v3.display.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.display.forms.LoginActions

@Composable
fun ColumnScope.LinkedServicesField(
    linkedServices: CredentialFormData.LinkedServices,
    editMode: Boolean,
    isEditable: Boolean,
    loginActions: LoginActions
) {
    if (!isEditable) {
        
        return
    }
    val linkButtonAlignment = if (!editMode) Modifier else Modifier.align(Alignment.End)
    if (linkedServices.size > 0) {
        LinkButton(
            modifier = linkButtonAlignment,
            text = pluralStringResource(
                id = R.plurals.vault_linked_services_manage,
                count = linkedServices.size,
                linkedServices.size
            ),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = loginActions.onLinkedServicesOpen,
        )
    } else if (editMode) {
        LinkButton(
            modifier = linkButtonAlignment,
            text = stringResource(id = R.string.vault_linked_services_add),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = loginActions.onLinkedServicesOpen,
        )
    }
}

@Preview
@Composable
private fun PasswordFieldPreview() {
    val linkedServices = CredentialFormData.LinkedServices(
        addedByDashlaneApps = listOf("App 1"),
        addedByDashlaneDomains = listOf("google.com"),
        addedByUserApps = listOf("App 2"),
        addedByUserDomains = listOf("example.com", "test.com")
    )
    DashlanePreview {
        Column {
            LinkedServicesField(
                linkedServices = linkedServices,
                editMode = true,
                isEditable = true,
                loginActions = LoginActions()
            )
        }
    }
}