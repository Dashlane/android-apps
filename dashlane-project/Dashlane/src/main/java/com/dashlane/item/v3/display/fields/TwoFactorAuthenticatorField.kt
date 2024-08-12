package com.dashlane.item.v3.display.fields

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.authenticator.Hotp
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.Totp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.HOTPDisplayField
import com.dashlane.design.component.HOTPField
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.TOTPDisplayField
import com.dashlane.design.component.TOTPField
import com.dashlane.design.component.resetCountdown
import com.dashlane.design.component.startCountdown
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.DashlanePreview
import kotlinx.coroutines.delay

@Composable
fun TwoFactorAuthenticatorField(
    editMode: Boolean,
    isEditable: Boolean,
    otp: Otp?,
    onSetupTwoFactorAuthenticator: () -> Unit,
    onRemoveTwoFactorAuthenticator: () -> Unit,
    onValueCopy: (() -> Unit)?,
    onHotpRefreshed: (Hotp) -> Unit
) {
    when {
        editMode && otp == null && isEditable -> {
            TwoFactorAuthenticatorAddButton(onSetupTwoFactorAuthenticator)
        }
        otp != null -> {
            if (editMode) {
                TwoFactorAuthenticatorEdit(
                    otp,
                    isEditable,
                    onRemoveTwoFactorAuthenticator,
                    onHotpRefreshed
                )
            } else {
                TwoFactorAuthenticatorDisplay(otp, onValueCopy, onHotpRefreshed)
            }
        }
    }
}

@Composable
private fun TwoFactorAuthenticatorEdit(
    otp: Otp,
    isEditable: Boolean,
    onRemoveTwoFactorAuthenticator: () -> Unit,
    onHotpRefreshed: (Hotp) -> Unit
) {
    val fieldAction = if (isEditable) {
        FieldAction.Generic(
            iconLayout = ButtonLayout.IconOnly(
                iconToken = IconTokens.actionDeleteOutlined,
                contentDescription = stringResource(id = R.string.authenticator_item_edit_remove_action)
            ),
            onClick = {
                onRemoveTwoFactorAuthenticator()
                true
            }
        )
    } else {
        null
    }
    if (otp is Totp) {
        val pin = otp.getPin() ?: return
        var token by rememberSaveable { mutableStateOf(pin.code) }
        val animatedTimeRemaining = remember { Animatable(pin.timeRemaining.seconds.toFloat()) }
        TOTPField(
            modifier = Modifier.fillMaxWidth(),
            value = token,
            label = stringResource(id = R.string.login_totp_text_field_label),
            animatedTimeRemaining = animatedTimeRemaining,
            timeStep = pin.refreshInterval,
            action = fieldAction
        )
        LaunchedEffect(Unit) {
            while (true) {
                animatedTimeRemaining.startCountdown(animatedTimeRemaining.value.toInt())
                delay(animatedTimeRemaining.value.toInt() * 1_000L)
                val newPin = otp.getPin() ?: return@LaunchedEffect
                token = newPin.code
                animatedTimeRemaining.resetCountdown(newPin.refreshInterval)
            }
        }
    } else if (otp is Hotp) {
        val pin = otp.getPin() ?: return
        HOTPField(
            modifier = Modifier.fillMaxWidth(),
            value = pin.code,
            label = stringResource(id = R.string.login_totp_text_field_label),
            action = fieldAction,
            onRefreshClick = {
                onHotpRefreshed(otp)
                true
            }
        )
    }
}

@Composable
private fun TwoFactorAuthenticatorDisplay(
    otp: Otp,
    onValueCopy: (() -> Unit)?,
    onHotpRefreshed: (Hotp) -> Unit
) {
    if (otp is Totp) {
        val pin = otp.getPin() ?: return
        val animatedTimeRemaining = remember { Animatable(pin.timeRemaining.seconds.toFloat()) }
        var token by rememberSaveable { mutableStateOf(pin.code) }
        val refreshInterval = pin.refreshInterval
        TOTPDisplayField(
            modifier = Modifier.fillMaxWidth(),
            value = token,
            label = stringResource(id = R.string.login_totp_text_field_label),
            animatedTimeRemaining = animatedTimeRemaining,
            timeStep = refreshInterval,
            action1 = FieldAction.Generic(
                iconLayout = ButtonLayout.IconOnly(
                    iconToken = IconTokens.actionCopyOutlined,
                    contentDescription = stringResource(id = R.string.vault_action_copy) + " " + stringResource(
                        id = R.string.login_totp_text_field_label
                    )
                ),
                onClick = {
                    onValueCopy?.invoke()
                    true
                }
            ).takeIf { onValueCopy != null },
        )
        LaunchedEffect(Unit) {
            while (true) {
                animatedTimeRemaining.startCountdown(animatedTimeRemaining.value.toInt())
                delay(animatedTimeRemaining.value.toInt() * 1_000L)
                val newPin = otp.getPin() ?: return@LaunchedEffect
                token = newPin.code
                animatedTimeRemaining.resetCountdown(newPin.refreshInterval)
            }
        }
    } else if (otp is Hotp) {
        val pin = otp.getPin() ?: return
        HOTPDisplayField(
            modifier = Modifier.fillMaxWidth(),
            value = pin.code,
            label = stringResource(id = R.string.login_totp_text_field_label),
            action1 = FieldAction.Generic(
                iconLayout = ButtonLayout.IconOnly(
                    iconToken = IconTokens.actionCopyOutlined,
                    contentDescription = stringResource(id = R.string.vault_action_copy) + " " + stringResource(
                        id = R.string.login_totp_text_field_label
                    )
                ),
                onClick = {
                    onValueCopy?.invoke()
                    true
                }
            ).takeIf { onValueCopy != null },
            onRefreshClick = {
                onHotpRefreshed(otp)
                true
            }
        )
    }
}

@Composable
private fun TwoFactorAuthenticatorAddButton(onSetupTwoFactorAuthenticator: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        LinkButton(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            text = stringResource(id = R.string.vault_setup_2fa),
            destinationType = LinkButtonDestinationType.INTERNAL,
            onClick = onSetupTwoFactorAuthenticator
        )
    }
}

@Preview
@Composable
@Suppress("LongMethod")
private fun TwoFactorAuthenticatorFieldPreview() {
    DashlanePreview {
        Column {
            TwoFactorAuthenticatorField(
                editMode = true,
                isEditable = true,
                otp = Totp(secret = "azerty"),
                onSetupTwoFactorAuthenticator = {
                    
                },
                onRemoveTwoFactorAuthenticator = {
                    
                },
                onValueCopy = {
                    
                },
                onHotpRefreshed = {
                    
                }
            )
            TwoFactorAuthenticatorField(
                editMode = false,
                isEditable = true,
                otp = Totp(secret = "azer"),
                onSetupTwoFactorAuthenticator = {
                    
                },
                onRemoveTwoFactorAuthenticator = {
                    
                },
                onValueCopy = {
                    
                },
                onHotpRefreshed = {
                    
                }
            )
            TwoFactorAuthenticatorField(
                editMode = true,
                isEditable = false,
                otp = Hotp(secret = "qwerty"),
                onSetupTwoFactorAuthenticator = {
                    
                },
                onRemoveTwoFactorAuthenticator = {
                    
                },
                onValueCopy = {
                    
                },
                onHotpRefreshed = {
                    
                }
            )
            TwoFactorAuthenticatorField(
                editMode = true,
                isEditable = true,
                otp = null,
                onSetupTwoFactorAuthenticator = {
                    
                },
                onRemoveTwoFactorAuthenticator = {
                    
                },
                onValueCopy = {
                    
                },
                onHotpRefreshed = {
                    
                }
            )
            
            TwoFactorAuthenticatorField(
                editMode = false,
                isEditable = false,
                otp = Totp(secret = "azer"),
                onSetupTwoFactorAuthenticator = {
                    
                },
                onRemoveTwoFactorAuthenticator = {
                    
                },
                onValueCopy = null,
                onHotpRefreshed = {
                    
                }
            )
        }
    }
}