package com.dashlane.item.v3.display

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.LifecycleOwner
import com.dashlane.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.CreditCardFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData.Button.Action.LAUNCH_GUIDED_CHANGE
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.display.forms.credentialForm
import com.dashlane.item.v3.display.forms.secretForm
import com.dashlane.item.v3.display.forms.secureNoteForm
import com.dashlane.item.v3.display.sections.ItemDateSection
import com.dashlane.item.v3.viewmodels.CredentialItemEditViewModel
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecretItemEditViewModel
import com.dashlane.item.v3.viewmodels.SecureNoteItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.ui.action.Action
import com.dashlane.vault.summary.SummaryObject
import java.time.Clock

@Suppress("LongMethod")
@Composable
internal fun ItemEditScreen(
    activity: AppCompatActivity,
    viewModel: ItemEditViewModel<out FormData>,
    uiState: ItemEditState<out FormData>,
    lazyListState: LazyListState,
    navigator: Navigator,
    viewLifecycleOwner: LifecycleOwner,
    clock: Clock,
    resultLauncherAuthenticator: ActivityResultLauncher<HasLogins.CredentialItem>,
    resultLauncherAttachments: ActivityResultLauncher<SummaryObject>,
    actionsMenu: MutableMap<Action, MenuItem>,
) {
    val data = uiState.datas?.current
    val snackbarState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    val menuHost: MenuHost = activity
    val menuProvider = getMenuProvider(
        activity,
        viewModel,
        actionsMenu,
        uiState.menuActions
    )

    LaunchedEffect(key1 = menuProvider) {
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
    }
    BackHandler {
        viewModel.onBackPressed()
    }
    LazyColumn(
        modifier = Modifier
            .background(DashlaneTheme.colors.backgroundAlternate)
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 40.dp),
        state = lazyListState
    ) {
        if (!uiState.isEditMode) {
            data?.let {
                item {
                    ItemHeader(data = data)
                }
            }
        }
        displayInfoBox(uiState.infoBoxes, uiState.isEditMode) { infoBoxAction ->
            when (infoBoxAction) {
                LAUNCH_GUIDED_CHANGE -> (viewModel as? CredentialItemEditViewModel)?.actionOpenGuidedPasswordChange()
            }
        }
        data?.let {
            displayFormData(data, viewModel, uiState)
            item {
                ItemDateSection(
                    clock = clock,
                    data = data,
                    editMode = uiState.isEditMode
                )
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = viewModel) {
        viewModel.uiState.sideEffect.collect { sideEffect ->
            performSideEffect(
                context,
                sideEffect,
                navigator,
                resultLauncherAuthenticator,
                resultLauncherAttachments,
                activity,
                snackbarState,
                snackbarScope
            )
        }
    }
    uiState.itemAction?.let { action ->
        data?.let {
            PerformAction(
                data,
                action,
                viewModel,
                navigator,
                activity
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        DashlaneSnackbarWrapper {
            SnackbarHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                hostState = snackbarState,
            )
        }
    }
}

private fun LazyListScope.displayFormData(
    data: Data<out FormData>,
    viewModel: ItemEditViewModel<out FormData>,
    uiState: ItemEditState<out FormData>
) {
    @Suppress("UNCHECKED_CAST")
    when (data.formData) {
        is CredentialFormData -> credentialForm(
            viewModel = viewModel as CredentialItemEditViewModel,
            uiState = (uiState as ItemEditState<CredentialFormData>).toNonNullableState()
        )
        is SecureNoteFormData -> secureNoteForm(
            viewModel = viewModel as SecureNoteItemEditViewModel,
            uiState = (uiState as ItemEditState<SecureNoteFormData>).toNonNullableState()
        )
        is SecretFormData -> secretForm(
            viewModel = viewModel as SecretItemEditViewModel,
            uiState = (uiState as ItemEditState<SecretFormData>).toNonNullableState()
        )
        is CreditCardFormData -> Unit
    }
}

fun getMenuProvider(
    activity: AppCompatActivity,
    viewModel: ItemEditViewModel<out FormData>,
    currentMenuActions: MutableMap<Action, MenuItem>,
    menuActions: List<MenuAction>
) = object : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        currentMenuActions.clear()
        menu.clear()
        menuActions.forEach {
            val s = SpannableString(activity.getString(it.text))
            if (it.color != null) {
                s.setSpan(ForegroundColorSpan(activity.getColor(it.color)), 0, s.length, 0)
            }
            menu.add(s).apply {
                setShowAsAction(it.displayFlags)
                isCheckable = it.checkable
                setIcon(it.icon)
                val buttonColor = if (!it.enabled) {
                    activity.getColor(R.color.text_oddity_disabled)
                } else {
                    activity.getColor(R.color.text_neutral_standard)
                }
                icon = icon?.mutate()?.also { ic -> ic.setTint(buttonColor) }
            }.also { item ->
                if (it.enabled) {
                    currentMenuActions[it] = item
                }
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return selectMenuItem(activity, menuItem, viewModel, currentMenuActions)
    }
}

private fun selectMenuItem(
    activity: AppCompatActivity,
    item: MenuItem,
    viewModel: ItemEditViewModel<out FormData>,
    currentMenuActions: MutableMap<Action, MenuItem>
): Boolean {
    if (item.itemId == android.R.id.home) {
        viewModel.onBackPressed()
        return true
    }
    val menuAction = currentMenuActions.filter { it.value == item }
    menuAction.forEach {
        val action = it.key
        action.onClickAction(activity)
    }
    return menuAction.isNotEmpty()
}