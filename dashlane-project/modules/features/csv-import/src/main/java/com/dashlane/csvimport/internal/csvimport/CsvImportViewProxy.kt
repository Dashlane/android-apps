package com.dashlane.csvimport.internal.csvimport

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.dashlane.csvimport.CsvImportViewTypeProvider
import com.dashlane.csvimport.ImportAuthentifiantHelper
import com.dashlane.csvimport.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.ui.widgets.view.empty.EmptyScreenConfiguration
import com.dashlane.ui.widgets.view.empty.EmptyScreenViewProvider
import com.skocken.presentation.viewproxy.BaseViewProxy

internal class CsvImportViewProxy(
    activity: Activity,
    private val csvImportViewTypeProviderFactory: CsvImportViewTypeProvider.Factory,
    private val authentifiantHelper: ImportAuthentifiantHelper
) : BaseViewProxy<CsvImportContract.Presenter>(activity),
    CsvImportContract.ViewProxy {
    private val title = findViewByIdEfficient<TextView>(R.id.title)!!

    private val progress = findViewByIdEfficient<View>(R.id.progress)!!

    private val adapter
        get() = recyclerView.adapter as DashlaneRecyclerAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>

    private val recyclerView = findViewByIdEfficient<MultiColumnRecyclerView>(R.id.recycler_view)!!.apply {
        setHasFixedSize(true)
    }

    private val emptyViewProvider = EmptyScreenViewProvider(
        EmptyScreenConfiguration.Builder()
            .setImage(VectorDrawableCompat.create(resources, R.drawable.ic_empty_password, null))
            .setLine1(context.getString(R.string.csv_import_empty_state_heading))
            .setLine2(context.getString(R.string.csv_import_empty_state_description))
            .build()
    )

    private val primaryCta = findViewByIdEfficient<Button>(R.id.primary_cta)!!.apply {
        setOnClickListener { presenter.onPrimaryCtaClicked() }
    }

    private val secondaryCta = findViewByIdEfficient<Button>(R.id.secondary_cta)!!.apply {
        setOnClickListener { presenter.onSecondaryCtaClicked() }
    }

    init {
        adapter.setOnItemClickListener { _, _, item, position ->
            if (item is CsvImportViewTypeProvider) {
                presenter.toggleAuthentifiantSelection(position)
            }
        }
    }

    override fun showLoading() {
        title.text = ""
        primaryCta.text = ""
        secondaryCta.text = ""

        recyclerView.visibility = View.GONE
        progress.visibility = View.VISIBLE
        primaryCta.visibility = View.INVISIBLE
        secondaryCta.visibility = View.INVISIBLE
    }

    override fun showEmptyState() {
        adapter.clear()
        adapter.add(emptyViewProvider)

        title.setText(R.string.csv_import_empty_state_title)
        primaryCta.setText(R.string.csv_import_empty_state_primary_cta)
        secondaryCta.setText(R.string.csv_import_empty_state_secondary_cta)

        recyclerView.visibility = View.VISIBLE
        progress.visibility = View.GONE
        primaryCta.visibility = View.VISIBLE
        secondaryCta.visibility = View.VISIBLE
    }

    override fun showList(items: List<CsvAuthentifiant>) {
        val lastInstanceState = recyclerView.gridLayoutManager.onSaveInstanceState()

        adapter.populateItems(
            items.map { csvAuthentifiant ->
                val vaultItem = csvAuthentifiant.toVaultItem(authentifiantHelper)
                csvImportViewTypeProviderFactory.create(vaultItem, csvAuthentifiant.selected)
            }
        )

        recyclerView.gridLayoutManager.onRestoreInstanceState(lastInstanceState)

        title.text = context.resources.getQuantityString(
            R.plurals.csv_import_title,
            items.size,
            items.size
        )

        primaryCta.isEnabled = items.any { it.selected }
        primaryCta.setText(R.string.csv_import_primary_cta)
        secondaryCta.setText(R.string.csv_import_secondary_cta)

        recyclerView.visibility = View.VISIBLE
        progress.visibility = View.GONE
        primaryCta.visibility = View.VISIBLE
        secondaryCta.visibility = View.VISIBLE
    }
}