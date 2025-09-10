package com.dashlane.ui.common.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashlane.design.component.Icon
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

@Suppress("LongMethod")
@Composable
fun ExpandableFaqItem(
    expanded: Boolean,
    onClick: () -> Unit,
    question: String,
    answer: String,
    linkButton: (@Composable ColumnScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val clickLabelToExpand = stringResource(R.string.and_accessibility_expandable_faq_item_click_label_to_expand)
    val clickLabelToCollapse = stringResource(R.string.and_accessibility_expandable_faq_item_click_label_to_collapse)
    val expandedStateDescription = stringResource(R.string.and_accessibility_expandable_faq_item_state_description_expanded)
    val collapsedStateDescription = stringResource(R.string.and_accessibility_expandable_faq_item_state_description_collapsed)
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = DashlaneTheme.colors.containerAgnosticNeutralSupershy)
            .clickable(
                onClickLabel = if (expanded) clickLabelToCollapse else clickLabelToExpand,
                onClick = onClick,
            )
            .semantics {
                stateDescription = if (expanded) expandedStateDescription else collapsedStateDescription
                if (expanded) {
                    liveRegion = LiveRegionMode.Polite
                }
            }
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f)
                    .semantics { heading() },
                text = question,
                color = DashlaneTheme.colors.textNeutralCatchy,
                style = DashlaneTheme.typography.titleBlockMedium,
            )
            val iconRotation: Float by animateFloatAsState(
                targetValue = if (expanded) 0f else 180f,
                label = "caret rotation",
            )
            Icon(
                token = IconTokens.caretDownOutlined,
                contentDescription = null,
                modifier = Modifier
                    .size(with(LocalDensity.current) { 20.sp.toDp() })
                    .rotate(iconRotation),
                tint = DashlaneTheme.colors.textNeutralStandard,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + slideOutVertically() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column {
                Text(
                    text = answer,
                    color = DashlaneTheme.colors.textNeutralStandard,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                )
                if (linkButton != null) {
                    linkButton()
                } else {
                    Spacer(
                        modifier = Modifier.height(8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ExpandableFaqItemPreview() {
    DashlanePreview {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var expandedItemIndex: Int by rememberSaveable { mutableIntStateOf(0) }
            ExpandableFaqItem(
                modifier = Modifier.fillMaxWidth(),
                expanded = expandedItemIndex == 0,
                onClick = { expandedItemIndex = if (expandedItemIndex == 0) -1 else 0 },
                question = "Question 1?",
                answer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua.",
                linkButton = {
                    LinkButton(
                        text = "Learn more",
                        destinationType = LinkButtonDestinationType.EXTERNAL,
                        onClick = {},
                    )
                },
            )
            ExpandableFaqItem(
                modifier = Modifier.fillMaxWidth(),
                expanded = expandedItemIndex == 1,
                onClick = { expandedItemIndex = if (expandedItemIndex == 1) -1 else 1 },
                question = "Question 2?",
                answer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                    "tempor incididunt ut labore et dolore magna aliqua.",
                linkButton = null,
            )
        }
    }
}