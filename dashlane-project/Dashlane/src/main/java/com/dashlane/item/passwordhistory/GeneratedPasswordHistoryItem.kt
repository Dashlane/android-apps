package com.dashlane.item.passwordhistory

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun GeneratedPasswordHistoryItem(
    modifier: Modifier = Modifier,
    password: String,
    dateString: String,
    domain: String?,
    saved: Boolean,
    onCopyClick: () -> Boolean,
) {
    val feedback = when {
        saved && domain != null -> stringResource(R.string.generated_password_history_feedback_saved, domain, dateString)
        domain != null -> stringResource(R.string.generated_password_history_feedback, domain, dateString)
        else -> stringResource(R.string.generated_password_history_feedback_default, dateString)
    }
    PasswordHistoryField(
        modifier = modifier,
        password = password,
        dateString = dateString,
        feedback = feedback,
        onCopyClick = onCopyClick,
    )
}

@SuppressLint("InternalTestExpressions")
@Composable
@Preview
private fun PasswordGeneratedHistoryItemPreview() {
    DashlanePreview {
        GeneratedPasswordHistoryItem(
            password = "Azerty12",
            dateString = "3 days ago",
            domain = "google.com",
            saved = false,
            onCopyClick = { true },
        )
    }
}