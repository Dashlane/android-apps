package com.dashlane.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.InfoboxButton
import com.dashlane.design.component.InfoboxLarge
import com.dashlane.design.component.Text
import com.dashlane.design.component.Toggle
import com.dashlane.design.component.cardBackground
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashlaneLabsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    DashlaneLabsScreen()
                }
            }
        }
    }

    @Composable
    fun DashlaneLabsScreen(viewModel: DashlaneLabsViewModel = viewModel()) {
        val menuHost: MenuHost = requireActivity()
        val menuProvider = remember {
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.help_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.menu_help) {
                        viewModel.onHelpClicked()
                        return true
                    }
                    return false
                }
            }
        }
        LaunchedEffect(key1 = menuProvider) {
            menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        if (uiState.viewData.helpClicked) {
            HelpDialog(viewModel)
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    InfoboxLarge(
                        title = stringResource(R.string.dashlane_labs_feedback_title),
                        description = stringResource(R.string.dashlane_labs_feedback_description),
                        mood = Mood.Neutral,
                        primaryButton = InfoboxButton(
                            text = stringResource(R.string.dashlane_labs_feedback_share),
                            onClick = {
                                
                                val feedbackForm = "https://forms.gle/SpSK8aNZQaBmMx3D6".toUri()
                                requireContext().launchUrl(feedbackForm)
                            }
                        )
                    )
                }
                when (uiState) {
                    is DashlaneLabsState.Loaded -> {
                        items(uiState.viewData.labFeatures) {
                            FeatureItem(
                                toggleClicked = { enable -> viewModel.toggleLabFeature(it.featureName, enable) },
                                feature = it
                            )
                        }
                    }
                    is DashlaneLabsState.Loading -> {
                        item {
                            IndeterminateLoader()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FeatureItem(toggleClicked: (Boolean) -> Unit, feature: ViewData.Lab) {
        Row(
            modifier = Modifier
                .cardBackground()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = feature.displayName,
                    style = DashlaneTheme.typography.titleBlockMedium,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = feature.displayDescription,
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Toggle(
                modifier = Modifier.padding(start = 16.dp),
                checked = feature.enabled,
                onCheckedChange = toggleClicked
            )
        }
    }

    @Composable
    @Preview
    private fun FeatureItemPreview() {
        DashlanePreview {
            FeatureItem(
                toggleClicked = {},
                feature = ViewData.Lab(
                    featureName = "",
                    displayName = "Feature name",
                    displayDescription = "Feature description",
                    enabled = true
                )
            )
        }
    }

    @Composable
    fun HelpDialog(viewModel: DashlaneLabsViewModel) {
        Dialog(
            title = stringResource(id = R.string.dashlane_labs_title),
            description = {
                Text(text = stringResource(id = R.string.dashlane_labs_description))
            },
            mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.and_accessibility_close)),
            mainActionClick = { viewModel.onHelpOpened() },
            onDismissRequest = { viewModel.onHelpOpened() }
        )
    }
}