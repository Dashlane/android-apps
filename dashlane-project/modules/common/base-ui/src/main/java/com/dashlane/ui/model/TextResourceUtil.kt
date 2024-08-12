package com.dashlane.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TextResource.getText(): String {
    val resources = LocalContext.current.resources
    return this.format(resources)
}