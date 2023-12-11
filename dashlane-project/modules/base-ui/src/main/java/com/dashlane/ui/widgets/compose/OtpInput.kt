package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("kotlin:S3776", "LongMethod", "ComplexMethod") 
@Composable
fun OtpInput(
    modifier: Modifier = Modifier,
    otp: String? = null, 
    isError: Boolean,
    error: String? = null,
    onOtpComplete: (String) -> Unit
) {
    var otpChars by rememberSaveable(otp) { mutableStateOf(otp?.toList()?.map { it.toString() } ?: listOf("", "", "", "", "", "")) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = focusRequester) {
        focusRequester.requestFocus()
    }

    Column {
        LazyRow(
            modifier = modifier.semantics {
                testTagsAsResourceId = true
            }.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            itemsIndexed(otpChars) { index, text ->
                val textFieldModifier = if (index == 0) modifier.focusRequester(focusRequester) else modifier

                BasicTextField(
                    modifier = textFieldModifier
                        .testTag("OTPInput$index")
                        .fillParentMaxWidth(1f.div(otpChars.size) - 0.03f) 
                        .onKeyEvent { event ->
                            
                            val isBackSpace = event.type == KeyEventType.KeyUp && event.key == Key.Backspace
                            val previousIndex = index - 1
                            if (!isBackSpace || previousIndex == -1) return@onKeyEvent false
                            val previousCharIsNotBlank = otpChars[previousIndex].isNotBlank()

                            return@onKeyEvent if (text.isBlank() && previousCharIsNotBlank) {
                                otpChars = otpChars.toMutableList().apply { set(previousIndex, "") }
                                focusManager.moveFocus(FocusDirection.Previous)
                                true
                            } else {
                                false
                            }
                        },
                    value = text,
                    onValueChange = { newValue ->
                        
                        if (newValue.isNotEmpty() && newValue.toIntOrNull() == null) return@BasicTextField
                        otpChars = if (newValue.length == 1 || newValue.isEmpty()) {
                            otpChars.toMutableList().apply { set(index, newValue) }
                        } else {
                            
                            otpChars.toMutableList().apply { set(index, newValue.last().toString()) }
                        }
                        if (newValue.isNotEmpty()) {
                            
                            val otp = otpChars.joinToString("")
                            if (otp.length == 6) onOtpComplete(otp)
                            if (index != otpChars.lastIndex) focusManager.moveFocus(FocusDirection.Next)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    textStyle = TextStyle.Default.copy(
                        fontSize = 28.sp,
                        color = DashlaneTheme.colors.textNeutralCatchy.value,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(DashlaneTheme.colors.textNeutralStandard.value),
                ) { innerTextField ->
                    TextFieldDefaults.TextFieldDecorationBox(
                        value = text,
                        visualTransformation = VisualTransformation.None,
                        innerTextField = innerTextField,
                        singleLine = true,
                        enabled = true,
                        isError = isError,
                        interactionSource = remember { MutableInteractionSource() },
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = DashlaneTheme.colors.textBrandStandard.value,
                            focusedIndicatorColor = DashlaneTheme.colors.borderBrandStandardActive,
                            unfocusedIndicatorColor = DashlaneTheme.colors.borderBrandStandardIdle,
                            errorIndicatorColor = DashlaneTheme.colors.borderDangerStandardIdle
                        )
                    )
                }
                if (index != otpChars.lastIndex) Spacer(Modifier.weight(0.5f))
            }
        }
        if (isError && error != null) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = error,
                color = DashlaneTheme.colors.textDangerQuiet,
                style = DashlaneTheme.typography.bodyHelperRegular
            )
        }
    }
}

@Preview
@Composable
fun OtpInputPreview() {
    DashlanePreview {
        OtpInput(onOtpComplete = { }, isError = true, error = "Error")
    }
}