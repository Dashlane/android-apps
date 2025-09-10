package com.dashlane.item.v3.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.dashlane.BuildConfig
import com.dashlane.R
import com.dashlane.common.logger.DeveloperLogAction
import com.dashlane.common.logger.Logs
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.Data

@Suppress("kotlin:S3776")
internal fun ComposeView.setupToolbar(
    currentData: Data<out FormData>,
    isNameVisible: Boolean,
    isEditMode: Boolean,
    isNew: Boolean,
) = setContent {
    DashlaneTheme {
        if (isEditMode) {
            Text(
                text = if (isNew) {
                    when (currentData.formData) {
                        is CredentialFormData -> {
                            stringResource(id = R.string.action_bar_title_credential_create_step1)
                        }
                        is SecureNoteFormData -> stringResource(id = R.string.vault_toolbar_secure_note_create_title)
                        is SecretFormData -> stringResource(id = R.string.vault_toolbar_secret_create_title)
                        is CreditCardFormData -> stringResource(id = R.string.vault_toolbar_credit_card_create_title)
                        is WiFiFormData -> stringResource(id = R.string.vault_toolbar_wifi_create_title)
                        else -> {
                            val unhandledType = currentData.formData::class.java.simpleName
                            val errorMessage =
                                "Unhandled $unhandledType form data type. Unable to set a title for adding items of that type."
                                message = errorMessage,
                                customLogs = listOf(
                                    Logs.Kibana(
                                        username = null,
                                        action = DeveloperLogAction.UNKNOWN_VAULT_TOOLBAR_TYPE,
                                    ),
                                ),
                            )
                            if (BuildConfig.DEBUG) {
                                error(errorMessage)
                            }
                            stringResource(R.string.vault_toolbar_unknown_type)
                        }
                    }
                } else {
                    stringResource(id = R.string.edit)
                },
                modifier = Modifier.semantics { heading() },
                style = DashlaneTheme.typography.titleSectionMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            AnimatedVisibility(
                visible = isNameVisible,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(1000)),
            ) {
                Text(
                    text = currentData.commonData.name ?: "",
                    modifier = Modifier.semantics { heading() },
                    style = DashlaneTheme.typography.titleSectionMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}