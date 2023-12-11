package com.dashlane.csvimport.csvimport.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.dashlane.csvimport.R
import com.dashlane.csvimport.csvimport.CsvAuthentifiant
import com.dashlane.csvimport.csvimport.CsvImportViewTypeProvider
import com.dashlane.csvimport.csvimport.CsvSchema
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.csvimport.csvimport.toVaultItem
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity
import com.dashlane.csvimport.databinding.ActivityCsvImportBinding
import com.dashlane.csvimport.utils.Intents
import com.dashlane.csvimport.utils.localBroadcastManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.empty.EmptyScreenConfiguration
import com.dashlane.ui.widgets.view.empty.EmptyScreenViewProvider
import com.dashlane.util.ActivityResultContractCompat
import com.dashlane.util.showToaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CsvImportActivity : DashlaneActivity() {

    @Inject
    lateinit var csvImportViewTypeProviderFactory: CsvImportViewTypeProvider.Factory

    @Inject
    lateinit var importAuthentifiantHelper: ImportAuthentifiantHelper

    private lateinit var adapter: DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>

    private val viewModel: CsvImportViewModel by viewModels()
    private lateinit var binding: ActivityCsvImportBinding
    private val matchCsvFieldsActivity =
        registerForActivityResult(object : ActivityResultContractCompat<List<String>>() {
            override fun createIntent(context: Context, input: List<String>): Intent =
                MatchCsvFieldsActivity.newIntent(
                    this@CsvImportActivity,
                    input
                )
        }) { (resultCode, intent) ->
            val fieldTypes = intent?.getIntegerArrayListExtra(MatchCsvFieldsActivity.EXTRA_CATEGORIES)?.map {
                when (it) {
                    MatchCsvFieldsActivity.CATEGORY_URL -> CsvSchema.FieldType.URL
                    MatchCsvFieldsActivity.CATEGORY_USERNAME -> CsvSchema.FieldType.USERNAME
                    MatchCsvFieldsActivity.CATEGORY_PASSWORD -> CsvSchema.FieldType.PASSWORD
                    else -> null
                }
            }

            if (resultCode == Activity.RESULT_OK && fieldTypes != null) {
                viewModel.matchFields(fieldTypes)
            } else {
                broadcastResultAndFinish(Intents.CSV_IMPORT_RESULT_FAILURE)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCsvImportBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapter = binding.recyclerView.adapter as DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>
        adapter.setOnItemClickListener { _, _, item, position ->
            if (item is CsvImportViewTypeProvider) {
                viewModel.toggleAuthentifiantSelection(position)
            }
        }
        binding.recyclerView.setHasFixedSize(true)
        binding.primaryCta.setOnClickListener {
            viewModel.onPrimaryCtaClicked()
        }
        binding.secondaryCta.setOnClickListener {
            viewModel.onSecondaryCtaClicked()
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    handleState(it)
                }
            }
        }
    }

    private fun handleState(state: CsvImportState) {
        when (state) {
            is CsvImportState.Loading -> showLoading()
            is CsvImportState.Loaded -> {
                if (state.data.selectedCredentials.isEmpty()) {
                    showEmptyState()
                } else {
                    showList(state.data.selectedCredentials)
                }
            }
            is CsvImportState.Error -> {
                broadcastResultAndFinish(state.result)
            }
            is CsvImportState.Saved -> {
                showToaster(
                    resources.getQuantityString(R.plurals.csv_import_success_message, state.count, state.count),
                    Toast.LENGTH_SHORT
                )
                broadcastResultAndFinish(Intents.CSV_IMPORT_RESULT_SUCCESS)
            }
            is CsvImportState.OpenMatchActivity -> {
                matchCsvFieldsActivity.launch(state.fields)
                viewModel.matchActivityOpen(state.separator)
            }
            is CsvImportState.Initial, is CsvImportState.Matching -> Unit
        }
    }

    private fun showLoading() {
        binding.title.text = ""
        binding.primaryCta.text = ""
        binding.secondaryCta.text = ""

        binding.recyclerView.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.primaryCta.visibility = View.INVISIBLE
        binding.secondaryCta.visibility = View.INVISIBLE
    }

    private fun showEmptyState() {
        adapter.clear()
        adapter.add(
            EmptyScreenViewProvider(
                EmptyScreenConfiguration.Builder()
                    .setImage(VectorDrawableCompat.create(resources, R.drawable.ic_empty_password, null))
                    .setLine1(getString(R.string.csv_import_empty_state_heading))
                    .setLine2(getString(R.string.csv_import_empty_state_description)).build()
            )
        )

        binding.title.setText(R.string.csv_import_empty_state_title)
        binding.primaryCta.setText(R.string.csv_import_empty_state_primary_cta)
        binding.secondaryCta.setText(R.string.csv_import_empty_state_secondary_cta)

        binding.recyclerView.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        binding.primaryCta.visibility = View.VISIBLE
        binding.secondaryCta.visibility = View.VISIBLE
    }

    private fun showList(items: List<CsvAuthentifiant>) {
        val lastInstanceState = binding.recyclerView.gridLayoutManager.onSaveInstanceState()

        adapter.populateItems(
            items.map { csvAuthentifiant ->
                val vaultItem = csvAuthentifiant.toVaultItem(this.importAuthentifiantHelper)
                csvImportViewTypeProviderFactory.create(vaultItem, csvAuthentifiant.selected)
            }
        )

        binding.recyclerView.gridLayoutManager.onRestoreInstanceState(lastInstanceState)

        binding.title.text = resources.getQuantityString(R.plurals.csv_import_title, items.size, items.size)

        binding.primaryCta.isEnabled = items.any { it.selected }
        binding.primaryCta.setText(R.string.csv_import_primary_cta)
        binding.secondaryCta.setText(R.string.csv_import_secondary_cta)

        binding.recyclerView.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        binding.primaryCta.visibility = View.VISIBLE
        binding.secondaryCta.visibility = View.VISIBLE
    }

    private fun broadcastResultAndFinish(@Intents.CsvImportResult result: String) {
        val csvImportIntent = Intent(Intents.ACTION_CSV_IMPORT).putExtra(Intents.EXTRA_CSV_IMPORT_RESULT, result)

        localBroadcastManager.sendBroadcast(csvImportIntent)
        finish()
    }

    companion object {
        const val EXTRA_URI = "uri"

        @JvmStatic
        fun newIntent(context: Context, uri: Uri): Intent =
            DashlaneIntent.newInstance(context, CsvImportActivity::class.java).putExtra(EXTRA_URI, uri)
    }
}