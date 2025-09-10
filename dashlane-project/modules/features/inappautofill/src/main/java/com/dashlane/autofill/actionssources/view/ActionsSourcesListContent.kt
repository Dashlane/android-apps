package com.dashlane.autofill.actionssources.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.view.AutofillFormSourceThumbnail
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ListItemActions
import com.dashlane.design.component.Text
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun ActionsSourcesListContent(
    temporarilyPausedItems: List<PausedItemData>,
    permanentlyPausedItems: List<PausedItemData>,
    onResumeClick: (AutoFillFormSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val temporarilyHeader = stringResource(R.string.paused_autofill_setting_temporarily_paused_header)
    val permanentlyHeader = stringResource(R.string.paused_autofill_setting_permanently_paused_header)
    LazyColumn(
        modifier = modifier
            .widthIn(max = 480.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(16.dp),
    ) {
        if (temporarilyPausedItems.isNotEmpty()) {
            item(key = "temporarily paused section header") {
                TopSectionHeader(temporarilyHeader)
            }
            items(temporarilyPausedItems, key = { item -> item.form }) { item ->
                ActionsSourceItem(item = item, onResumeClick = onResumeClick)
            }
            item(key = "temporarily paused section bottom space") {
                BottomSectionCard()
            }
        }
        if (temporarilyPausedItems.isNotEmpty() && permanentlyPausedItems.isNotEmpty()) {
            item(key = "space between section") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (permanentlyPausedItems.isNotEmpty()) {
            item(key = "permanently paused section header") {
                TopSectionHeader(permanentlyHeader)
            }
            items(permanentlyPausedItems, key = { item -> item.form }) { item ->
                ActionsSourceItem(item = item, onResumeClick = onResumeClick)
            }
            item(key = "permanently paused section bottom space") {
                BottomSectionCard()
            }
        }
    }
}

@Composable
private fun TopSectionHeader(
    header: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .semantics { heading() }
            .fillMaxWidth()
            .cardBackground(isTop = true, isBottom = false)
            .padding(top = 16.dp, bottom = 8.dp)
            .padding(horizontal = 16.dp),
        text = header,
        style = DashlaneTheme.typography.titleBlockMedium,
        color = DashlaneTheme.colors.textNeutralCatchy,
    )
}

@Composable
private fun ActionsSourceItem(
    item: PausedItemData,
    modifier: Modifier = Modifier,
    onResumeClick: (AutoFillFormSource) -> Unit,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(isTop = false, isBottom = false)
            .padding(start = 8.dp, end = 12.dp),
        thumbnail = {
            AutofillFormSourceThumbnail(icon = item.icon)
        },
        title = item.title,
        description = item.description,
        onClick = null,
        actions = PausedItemActions(
            resumeLabel = stringResource(R.string.paused_autofill_setting_re_enable_action),
            onClick = { onResumeClick(item.form) },
        ),
    )
}

@Composable
private fun BottomSectionCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .cardBackground(isTop = false, isBottom = true),
    )
}

internal class PausedItemActions(
    private val resumeLabel: String,
    private val onClick: () -> Unit,
) : ListItemActions {
    @Composable
    override fun Content() {
        ButtonMedium(
            modifier = Modifier.semantics { invisibleToUser() },
            layout = ButtonLayout.TextOnly(resumeLabel),
            intensity = Intensity.Supershy,
            onClick = onClick,
        )
    }

    override fun getCustomAccessibilityActions(): List<CustomAccessibilityAction> {
        return listOf(
            CustomAccessibilityAction(
                label = resumeLabel,
                action = {
                    onClick()
                    true
                },
            ),
        )
    }
}

@Preview
@Composable
private fun PausedAutofillListContentPreview() {
    DashlanePreview {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            ActionsSourcesListContent(
                temporarilyPausedItems = listOf(
                    PausedItemData(
                        form = WebDomainFormSource("", "twitch.tv"),
                        icon = AutofillFormSourceIcon.WebsiteUrl(""),
                        title = "twitch.tv",
                        description = "36 minutes left",
                    ),
                    PausedItemData(
                        form = WebDomainFormSource("", "n26.com"),
                        icon = AutofillFormSourceIcon.WebsiteUrl(""),
                        title = "n26.com",
                        description = "17 hours left",
                    ),
                ),
                permanentlyPausedItems = listOf(
                    PausedItemData(
                        form = WebDomainFormSource("", "spotify.com"),
                        icon = AutofillFormSourceIcon.WebsiteUrl(""),
                        title = "spotify.com",
                        description = null,
                    ),
                    PausedItemData(
                        form = ApplicationFormSource(""),
                        icon = AutofillFormSourceIcon.NotInstalledApplication(),
                        title = "Autofill demo",
                        description = null,
                    ),
                ),
                onResumeClick = {},
            )
        }
    }
}