package com.dashlane.csvimport.matchcsvfields

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.IntDef
import com.dashlane.csvimport.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchCsvFieldsActivity : DashlaneActivity() {

    private val viewModel: MatchCsvFieldsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_csv_import)

        MatchCsvFieldsViewProxy(this, viewModel)

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
        const val EXTRA_CATEGORIES = "extra_categories"

        fun newIntent(
            context: Context,
            fields: List<String>
        ): Intent =
            Intent(context, MatchCsvFieldsActivity::class.java)
                .putExtra(EXTRA_FIELDS, ArrayList(fields))
    }
}