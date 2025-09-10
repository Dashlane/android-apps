package com.dashlane.csvimport.intro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.dashlane.csvimport.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.tooling.BooleanProvider
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreen
import com.dashlane.ui.common.compose.components.basescreen.BoardingScreenDefaults

@Composable
fun CsvFileImportIntroScreen(
    fromCompetitor: Boolean,
    goToCsvImport: (Uri) -> Unit,
    onCancelClick: () -> Unit,
    onSelectFileClicked: () -> Unit,
) {
    val selectCsvFileLauncher = rememberLauncherForActivityResult(SelectCsvFileResult()) { uri ->
        if (uri != null) {
            onSelectFileClicked()
            goToCsvImport(uri)
        }
    }

    if (fromCompetitor) {
        CsvFileImportFromCompetitorIntroContent(
            onSelectFiledClick = { selectCsvFileLauncher.launch(Unit) },
            onCancelClick = onCancelClick,
        )
    } else {
        CsvFileImportGenericIntroContent(
            onSelectFiledClick = { selectCsvFileLauncher.launch(Unit) },
            onCancelClick = onCancelClick,
        )
    }
}

@Composable
private fun CsvFileImportGenericIntroContent(
    onSelectFiledClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(IconTokens.csvOutlined),
        title = stringResource(R.string.csv_file_import_title),
        description = listOf(
            R.string.csv_file_import_description_line1,
            R.string.csv_file_import_description_line2,
            R.string.csv_file_import_description_line3,
        ).joinToString("\n") { resources.getString(it) },
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.csv_file_import_primary_cta),
            onPrimaryButtonClick = onSelectFiledClick,
            secondaryButtonText = stringResource(R.string.csv_file_import_secondary_cta),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Composable
private fun CsvFileImportFromCompetitorIntroContent(
    onSelectFiledClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    BoardingScreen(
        decoration = BoardingScreenDefaults.decoration(IconTokens.downloadOutlined),
        title = stringResource(R.string.csv_file_import_title_competitor),
        description = listOf(
            R.string.csv_file_import_description_competitor_line1,
            R.string.csv_file_import_description_competitor_line2,
            R.string.csv_file_import_description_competitor_line3,
        ).joinToString("\n") { resources.getString(it) },
        actions = BoardingScreenDefaults.Actions(
            primaryButtonText = stringResource(R.string.csv_file_import_competitor_primary_cta),
            onPrimaryButtonClick = onSelectFiledClick,
            secondaryButtonText = stringResource(R.string.csv_file_import_competitor_secondary_cta),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

private class SelectCsvFileResult : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "\1/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.data!!
        }
        return null
    }
}

@Preview
@Composable
private fun CsvFileImportIntroScreenPreview(
    @PreviewParameter(BooleanProvider::class) fromCompetitor: Boolean,
) {
    CsvFileImportIntroScreen(
        fromCompetitor = fromCompetitor,
        goToCsvImport = {},
        onCancelClick = {},
        onSelectFileClicked = {},
    )
}