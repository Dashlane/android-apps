package com.dashlane.ui.common.compose.components.basescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.dashlane.design.component.ButtonLargeBar
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults.ActionButtonSize.LARGE
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults.ActionButtonSize.MEDIUM
import com.dashlane.ui.common.compose.components.screentemplate.BoxColumn

@Composable
fun BoardingScreen(
    decoration: @Composable ColumnScope.() -> Unit,
    title: String,
    description: String,
    actions: BoardingScreenDefaults.Actions,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    actionButtonSize: BoardingScreenDefaults.ActionButtonSize = MEDIUM,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
    ) { scaffoldContentPadding ->
        BoardingContent(
            decoration = decoration,
            title = title,
            description = description,
            actions = actions,
            modifier = Modifier.padding(scaffoldContentPadding),
            contentPadding = contentPadding,
            actionButtonSize = actionButtonSize,
            extraContent = extraContent,
        )
    }
}

@Suppress("LongMethod", "kotlin:S107")
@Composable
fun BoardingContent(
    decoration: @Composable ColumnScope.() -> Unit,
    title: String,
    description: String,
    actions: BoardingScreenDefaults.Actions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    actionButtonSize: BoardingScreenDefaults.ActionButtonSize = MEDIUM,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val layoutDirection = LocalLayoutDirection.current
    BoxColumn(
        modifier = modifier,
        
        
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = 0.dp,
        ),
        breakpoint = 640.dp,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        decoration()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
        )
        extraContent?.invoke(this)
        Spacer(modifier = Modifier.weight(1f))
        if (actions.primaryButtonLayout != null || actions.secondaryButtonLayout != null) {
            when (actionButtonSize) {
                MEDIUM -> {
                    ButtonMediumBar(
                        modifier = Modifier
                            .padding(
                                top = 10.dp,
                                bottom = max(contentPadding.calculateBottomPadding() - 6.dp, 0.dp),
                            ),
                        primaryButtonLayout = actions.primaryButtonLayout,
                        secondaryButtonLayout = actions.secondaryButtonLayout,
                        onPrimaryButtonClick = actions.onPrimaryButtonClick,
                        onSecondaryButtonClick = actions.onSecondaryButtonClick,
                    )
                }

                LARGE -> {
                    ButtonLargeBar(
                        modifier = Modifier
                            .background(Color.Cyan)
                            .padding(
                                top = 16.dp,
                                bottom = contentPadding.calculateBottomPadding(),
                            ),
                        primaryButtonLayout = actions.primaryButtonLayout,
                        secondaryButtonLayout = actions.secondaryButtonLayout,
                        onPrimaryButtonClick = actions.onPrimaryButtonClick,
                        onSecondaryButtonClick = actions.onSecondaryButtonClick,
                    )
                }
            }
        } else {
            Spacer(
                modifier = Modifier.height(contentPadding.calculateBottomPadding()),
            )
        }
    }
}

object BoardingScreenDefaults {
    val maxWidth = 640.dp

    enum class ActionButtonSize {
        MEDIUM,
        LARGE,
    }

    fun decoration(
        iconToken: IconToken,
        error: Boolean = false,
    ): @Composable ColumnScope.() -> Unit = {
        ExpressiveIcon(
            icon = iconToken,
            size = ExpressiveIconSize.XLarge,
            mood = if (error) Mood.Danger else Mood.Brand,
        )
    }

    data class Actions(
        val primaryButtonLayout: ButtonLayout?,
        val onPrimaryButtonClick: (() -> Unit)?,
        val secondaryButtonLayout: ButtonLayout? = null,
        val onSecondaryButtonClick: (() -> Unit)? = null,
        val isDestructive: Boolean = false,
        val isPrimaryButtonEnabled: Boolean = true,
    ) {
        constructor(
            primaryButtonText: String?,
            onPrimaryButtonClick: (() -> Unit)?,
            secondaryButtonText: String? = null,
            onSecondaryButtonClick: (() -> Unit)? = null,
            isDestructive: Boolean = false,
            isPrimaryButtonEnabled: Boolean = true,
        ) : this(
            primaryButtonLayout = primaryButtonText?.let { ButtonLayout.TextOnly(it) },
            onPrimaryButtonClick = onPrimaryButtonClick,
            secondaryButtonLayout = secondaryButtonText?.let { ButtonLayout.TextOnly(it) },
            onSecondaryButtonClick = onSecondaryButtonClick,
            isDestructive = isDestructive,
            isPrimaryButtonEnabled = isPrimaryButtonEnabled,
        )
    }
}

@Preview
@Composable
private fun BoardingScreenPreview() {
    DashlanePreview {
        BoardingScreen(
            decoration = BoardingScreenDefaults.decoration(
                iconToken = IconTokens.itemDriversLicenseOutlined,
            ),
            title = "Title here",
            description = """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent rutrum dolor rutrum erat pretium, ut ullamcorper mi feugiat. Pellentesque nibh est, porta quis elit vitae, varius pretium sapien. Nunc arcu diam, lobortis id suscipit at, cursus ut lorem. Mauris at sollicitudin urna, ac pretium urna. Praesent feugiat ornare mattis. Duis consequat neque vitae neque gravida, in eleifend felis volutpat. Interdum et malesuada fames ac ante ipsum primis in faucibus. Phasellus eu magna quis magna congue ornare. Nunc feugiat nisi leo, ac mattis libero hendrerit vel. Ut tincidunt ut ex quis fringilla. Morbi ante arcu, aliquet sed erat quis, ultricies facilisis metus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Quisque auctor congue quam, vitae viverra lectus venenatis ut. Quisque id leo ligula. Nullam quis dui ut enim vulputate varius. Aliquam finibus vulputate tellus, ut cursus leo euismod in.
            """.trimIndent(),
            actions = BoardingScreenDefaults.Actions(
                primaryButtonText = "Positive",
                onPrimaryButtonClick = {},
                secondaryButtonText = "Negative",
                onSecondaryButtonClick = {},
            ),
        )
    }
}