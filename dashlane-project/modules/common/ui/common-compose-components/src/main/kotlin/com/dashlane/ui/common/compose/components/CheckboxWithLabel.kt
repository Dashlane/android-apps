package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Checkbox
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.setCheckboxSemantic
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun CheckboxWithLabel(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(4.dp))
            .setCheckboxSemantic(
                value = checked,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            modifier = Modifier.padding(5.dp),
            checked = checked,
        )
        Spacer(
            modifier = Modifier.width(4.dp),
        )
        Text(
            text = label,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
    }
}

@Preview
@Composable
private fun CheckboxWithLabelPreview() {
    DashlanePreview {
        var checked by remember { mutableStateOf(false) }
        CheckboxWithLabel(
            modifier = Modifier.fillMaxWidth(),
            label = "Do not show again",
            checked = checked,
            onClick = { checked = !checked },
        )
    }
}