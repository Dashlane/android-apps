package com.dashlane.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.R

@Composable
fun PassphraseLayout(
    modifier: Modifier = Modifier,
    passphrase: List<Passphrase>,
    onValueChange: ((String) -> Unit)?,
    onKeyboardDone: (() -> Unit)?
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        passphrase.forEach { word ->
            when (word) {
                is Passphrase.Missing -> PassphraseMissing(
                    value = word.userInput,
                    isError = word.isError,
                    onValueChange = onValueChange ?: throw IllegalStateException("onValueChange is needed for a passphrase with a missing word"),
                    onKeyboardDone = onKeyboardDone ?: throw IllegalStateException("onKeyboardDone is needed for a passphrase with a missing word")
                )
                is Passphrase.Word -> PassphraseWord(word.value)
            }
        }
    }
}

@Composable
private fun PassphraseWord(word: String) {
    Text(
        text = word,
        style = DashlaneTheme.typography.bodyReducedMonospace,
        color = DashlaneTheme.colors.textNeutralCatchy,
        modifier = Modifier
            .padding(start = 4.dp)
    )
}

@Composable
private fun PassphraseMissing(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onKeyboardDone: (() -> Unit)?
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = value,
        isError = isError,
        feedbackText = if (isError) stringResource(id = R.string.passphrase_component_missing_word_error) else null,
        onValueChange = onValueChange,
        keyboardActions = onKeyboardDone?.let { KeyboardActions(onDone = { onKeyboardDone() }) } ?: KeyboardActions.Default,
        label = stringResource(id = R.string.passphrase_component_missing_word_label)
    )
}

sealed class Passphrase {
    abstract val value: String
    data class Word(override val value: String) : Passphrase()
    data class Missing(override val value: String, val userInput: String, val isError: Boolean) : Passphrase()
}

@Preview
@Composable
private fun PassphraseLayoutReceiverPreview() {
    DashlanePreview {
        PassphraseLayout(
            passphrase = listOf(
                Passphrase.Word("carrot"),
                Passphrase.Word("whales"),
                Passphrase.Word("potatoes"),
                Passphrase.Word("plant"),
                Passphrase.Word("mascara")
            ),
            onValueChange = {},
            onKeyboardDone = {}
        )
    }
}

@Preview
@Composable
private fun PassphraseLayoutSenderPreview() {
    DashlanePreview {
        PassphraseLayout(
            passphrase = listOf(
                Passphrase.Word("carrot"),
                Passphrase.Word("whales"),
                Passphrase.Word("potatoes"),
                Passphrase.Missing(value = "plant", userInput = "", isError = true),
                Passphrase.Word("mascara")
            ),
            onValueChange = {},
            onKeyboardDone = {}
        )
    }
}