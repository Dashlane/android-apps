package com.dashlane.csvexport.intro

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink

class CsvExportFromWebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CsvExportFromWebScreen(
                    onBackNavigationClick = ::finish
                ) {
                    HelpCenterCoordinator.openLink(this, HelpCenterLink.ARTICLE_CSV_EXPORT_WEB)
                }
            }
        }
    }
}