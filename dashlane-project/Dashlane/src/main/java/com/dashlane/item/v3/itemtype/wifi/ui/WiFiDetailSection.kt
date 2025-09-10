package com.dashlane.item.v3.itemtype.wifi.ui

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.ItemEditObfuscatedField
import com.dashlane.item.v3.display.fields.ItemEditSuggestionField
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiItemEditAction
import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.rememberIsFieldVisible
import com.dashlane.item.v3.util.rememberIsSensitiveFieldRevealed
import com.dashlane.item.v3.viewmodels.Data

@SuppressLint("DiscouragedApi")
@Suppress("LongMethod", "kotlin:S107", "kotlin:S3776")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiDetailSection(
    data: Data<WiFiFormData>,
    hiddenFields: Set<Field>,
    revealedFields: Set<SensitiveField>,
    editMode: Boolean,
    handleAction: (WiFiItemEditAction) -> Unit,
) {
    SectionContent(showCard = !editMode) {
        SectionTitle(
            title = stringResource(id = R.string.wifi_detail_section),
            editMode = editMode,
        )
        ItemEditSuggestionField(
            label = stringResource(id = R.string.wifi_hint_ssid),
            data = data.formData.ssid,
            options = data.formData.suggestedNetworks,
            editMode = editMode,
            isRequired = true,
            isEditable = data.commonData.isEditable,
            keyboardType = KeyboardType.Unspecified,
            onValueChanged = { handleAction(WiFiItemEditAction.SsidChanged(it)) },
            onValueCopy = { handleAction(WiFiItemEditAction.CopySsid) }
                .takeIf { data.commonData.isCopyActionAllowed },
            imeAction = ImeAction.Next,
        )
        if (rememberIsFieldVisible(hiddenFields, Field.WIFI_PASSPHRASE)) {
            ItemEditObfuscatedField(
                label = stringResource(id = R.string.wifi_hint_passphrase),
                value = remember(data.formData.passphrase) {
                    data.formData.passphrase ?: ""
                },
                isEmpty = data.formData.isPassphraseEmpty,
                fixedHiddenValueLength = 8,
                onValueChange = { handleAction(WiFiItemEditAction.PassphraseChanged(it)) },
                onHideReveal = { handleAction(WiFiItemEditAction.RevealHidePassphrase) },
                onCopy = { handleAction(WiFiItemEditAction.CopyPassphrase) }
                    .takeIf { data.commonData.isCopyActionAllowed },
                readOnly = !data.commonData.isEditable,
                obfuscated = rememberIsSensitiveFieldRevealed(
                    revealedFields = revealedFields,
                    field = SensitiveField.WIFI_PASSPHRASE,
                ),
                editMode = editMode,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            )
        }
        if (rememberIsFieldVisible(hiddenFields, Field.WIFI_NOTE)) {
            GenericField(
                label = stringResource(id = R.string.wifi_hint_note),
                multiLine = true,
                data = data.formData.note,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                keyboardType = KeyboardType.Unspecified,
                onValueChanged = {
                    handleAction(WiFiItemEditAction.NoteChanged(it))
                },
                onValueCopy = { handleAction(WiFiItemEditAction.CopyNote) }
                    .takeIf { data.commonData.isCopyActionAllowed },
            )
        }

        if (editMode.not()) {
            LinkButton(
                text = stringResource(id = R.string.wifi_show_qr_code),
                destinationType = LinkButtonDestinationType.INTERNAL,
                onClick = {
                    handleAction(WiFiItemEditAction.ShowQRCodeBottomSheet)
                },
            )

            if (data.formData.isConnectToNetworkButtonVisible) {
                LinkButton(
                    text = stringResource(R.string.wifi_connect_to_network),
                    destinationType = LinkButtonDestinationType.INTERNAL,
                    onClick = {
                        handleAction(WiFiItemEditAction.ConnectToNetwork)
                    },
                )
            }

            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { sheetValue ->
                    if (sheetValue == SheetValue.Hidden) handleAction(WiFiItemEditAction.HideQRCodeBottomSheet)
                    true
                },
            )

            if (data.formData.isQRCodeBottomSheetVisible && data.formData.qrCodeContent != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        handleAction(WiFiItemEditAction.HideQRCodeBottomSheet)
                    },
                    sheetState = sheetState,
                ) {
                    WiFiQRCodeBottomSheet(
                        title = data.formData.ssid,
                        qrCodeContent = data.formData.qrCodeContent,
                        onDismiss = {
                            handleAction(WiFiItemEditAction.HideQRCodeBottomSheet)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun WiFiDetailSectionViewModePreview() = DashlanePreview {
    WiFiDetailSection(
        data = Data(
            commonData = CommonData(
                isCopyActionAllowed = true,
                isEditable = true,
                name = "My WiFi",
            ),
            formData = WiFiFormData(
                passphrase = null,
                isPassphraseEmpty = false,
            ),
        ),
        hiddenFields = emptySet(),
        revealedFields = emptySet(),
        editMode = false,
        handleAction = {},
    )
}

@Preview
@Composable
private fun WiFiDetailSectionEditModePreview() = DashlanePreview {
    WiFiDetailSection(
        data = Data(
            commonData = CommonData(
                isEditable = true,
            ),
            formData = WiFiFormData(),
        ),
        hiddenFields = emptySet(),
        revealedFields = emptySet(),
        editMode = true,
        handleAction = {},
    )
}