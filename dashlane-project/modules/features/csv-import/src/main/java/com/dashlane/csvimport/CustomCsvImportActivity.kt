package com.dashlane.csvimport

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.IntDef
import com.dashlane.csvimport.internal.customcsvimport.CustomCsvImportViewModel
import com.dashlane.csvimport.internal.customcsvimport.CustomCsvImportViewProxy
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomCsvImportActivity : DashlaneActivity() {

    private val viewModel: CustomCsvImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_csv_import)

        CustomCsvImportViewProxy(this, viewModel)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.onBackPressed()) {
                    finish()
                }
            }
        }
        )
    }

    @IntDef(
        value = [
            CATEGORY_NONE,
            CATEGORY_URL,
            CATEGORY_USERNAME,
            CATEGORY_PASSWORD
        ]
    )
    annotation class Category

    companion object {
        const val CATEGORY_NONE = 0
        const val CATEGORY_URL = 1
        const val CATEGORY_USERNAME = 2
        const val CATEGORY_PASSWORD = 3

        internal const val EXTRA_FIELDS = "extra_fields"
        internal const val EXTRA_ORIGIN = "extra_origin"
        const val EXTRA_CATEGORIES = "extra_categories"

        fun newIntent(
            context: Context,
            fields: List<String>,
            origin: String
        ): Intent =
            Intent(context, CustomCsvImportActivity::class.java)
                .putExtra(EXTRA_FIELDS, ArrayList(fields))
                .putExtra(EXTRA_ORIGIN, origin)
    }
}