package com.dashlane.collections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dashlane.ui.widgets.compose.IndeterminateLoading

@Composable
internal fun CollectionLoading() {
    Box(
        modifier = Modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        IndeterminateLoading(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    }
}