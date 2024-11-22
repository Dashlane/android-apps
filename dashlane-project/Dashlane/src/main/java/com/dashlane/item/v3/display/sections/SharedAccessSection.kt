package com.dashlane.item.v3.display.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle

@Composable
fun SharedAccessSection(
    commonData: CommonData,
    editMode: Boolean,
    onSharedClick: () -> Unit
) {
    val isShared = commonData.sharingCount.userCount > 0 || commonData.sharingCount.groupCount > 0
    if (editMode || !isShared) return
    SectionContent(editMode = editMode) {
        SectionTitle(title = stringResource(id = R.string.vault_shared_access), editMode = editMode)

        GenericField(
            label = stringResource(id = R.string.vault_shared_with),
            data = getSharingCount(sharingCount = commonData.sharingCount),
            editMode = false,
            onValueChanged = {}
        )
        LinkButton(
            text = stringResource(id = R.string.vault_view_all_shared_users),
            destinationType = LinkButtonDestinationType.INTERNAL
        ) {
            onSharedClick()
        }
    }
}

@Composable
private fun getSharingCount(sharingCount: FormData.SharingCount): String {
    val userCount = pluralStringResource(
        R.plurals.sharing_shared_counter_users,
        sharingCount.userCount,
        sharingCount.userCount
    )
    val groupCount = pluralStringResource(
        R.plurals.sharing_shared_counter_groups,
        sharingCount.groupCount,
        sharingCount.groupCount
    )
    return if (sharingCount.userCount != 0 && sharingCount.groupCount != 0) {
        stringResource(R.string.sharing_shared_shared_with_users_and_groups, userCount, groupCount)
    } else if (sharingCount.userCount != 0) {
        stringResource(R.string.sharing_shared_shared_with, userCount)
    } else {
        stringResource(R.string.sharing_shared_shared_with, groupCount)
    }
}

private val previewCommonData = CommonData(
    sharingCount = FormData.SharingCount(3, 1),
    isEditable = true
)

@Preview
@Composable
private fun SharedAccessSectionPreview() {
    DashlanePreview {
        SharedAccessSection(
           commonData = previewCommonData,
            editMode = false,
            onSharedClick = {}
        )
    }
}

@Preview
@Composable
private fun SharedAccessSectionEditPreview() {
    DashlanePreview {
        SharedAccessSection(
            commonData = previewCommonData,
            editMode = true,
            onSharedClick = {}
        )
    }
}