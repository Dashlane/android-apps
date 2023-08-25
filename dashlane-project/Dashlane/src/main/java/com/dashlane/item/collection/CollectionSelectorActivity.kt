package com.dashlane.item.collection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.navArgs
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Icon
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.ui.widgets.view.CategoryChip
import com.dashlane.ui.widgets.view.CollectionSearchField
import com.dashlane.util.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionSelectorActivity : AppCompatActivity() {

    val viewModel by viewModels<CollectionSelectorViewModel>()

    private val navArgs by navArgs<CollectionSelectorActivityArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CollectionSelector(viewModel)
            }
        }
    }

    @Composable
    @Suppress("LongMethod")
    fun CollectionSelector(
        viewModel: CollectionSelectorViewModel
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.collection_selector_activity_title),
                            style = DashlaneTheme.typography.titleSectionMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            DeviceUtils.hideKeyboard(this@CollectionSelectorActivity)
                            finish()
                        }) {
                            Icon(
                                token = IconTokens.actionCloseOutlined,
                                contentDescription = stringResource(id = R.string.and_accessibility_close),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    backgroundColor = DashlaneTheme.colors.containerAgnosticNeutralStandard
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(top = 16.dp, start = 16.dp, bottom = 0.dp, end = 16.dp)
            ) {
                val collections by viewModel.collections.collectAsState()
                val focusRequester = remember { FocusRequester() }
                CollectionSearchField(
                    prompt = viewModel.userPrompt,
                    onPromptChange = viewModel::updateUserPrompt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (viewModel.canCreate) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val label = viewModel.userPrompt
                                ButtonMedium(
                                    onClick = {
                                        onClickCollection(label)
                                    },
                                    layout = ButtonLayout.TextOnly(stringResource(id = R.string.collection_selector_activity_create_button)),
                                    intensity = Intensity.Quiet
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                CategoryChip(label = label) {
                                    onClickCollection(label)
                                }
                            }
                        }
                    }
                    items(collections.mapNotNull { it.name }) {
                        CategoryChip(label = it) {
                            onClickCollection(it)
                        }
                    }
                }
            }
        }
    }

    private fun onClickCollection(label: String) {
        val resultList = arrayOf(label)
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(RESULT_TEMPORARY_COLLECTIONS, resultList)
            }
        )
        DeviceUtils.hideKeyboard(this)
        finish()
    }

    override fun finish() {
        super.finish()
        if (!navArgs.fromView) {
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_bottom)
        }
    }

    companion object {
        const val SHOW_COLLECTION_SELECTOR = 3004
        const val RESULT_TEMPORARY_COLLECTIONS = "temporaryCollections"
    }
}