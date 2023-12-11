package com.dashlane.ui.menu.view.footer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.BaseButton
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun MenuLockFooterItem(onLockoutClick: () -> Unit) {
    val noCorner = CornerSize(0)
    BaseButton(
        onClick = onLockoutClick,
        layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.menu_v3_lock_app)),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        innerPadding = PaddingValues(horizontal = 36.dp, vertical = 16.dp),
        mood = Mood.Brand,
        intensity = Intensity.Quiet,
        enabled = true,
        shape = RoundedCornerShape(
            topStart = noCorner,
            topEnd = noCorner,
            bottomEnd = noCorner,
            bottomStart = noCorner
        ),
        animationSpec = null
    )
}

@Preview
@Composable
private fun MenuLockFooterItemPreview() {
    DashlanePreview {
        MenuLockFooterItem {}
    }
}