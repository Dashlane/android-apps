package com.dashlane.ui.screens.settings

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.UiConstants
import com.dashlane.util.applyAppTheme
import com.dashlane.util.fallbackCustomTab
import com.dashlane.util.readString
import com.dashlane.util.safelyStartBrowserActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken



class LicensesActivity : DashlaneActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.activity_title_licenses)
        }

        val licenses = loadLicenses()

        findViewById<RecyclerView>(R.id.view_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LicensesRecyclerView(
                licenses.values.toList(),
                layoutInflater,
                CachedLicenseAssets(assets)
            )
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val position = parent.getChildAdapterPosition(view)
                    if (position == 0) {
                        outRect.top += resources.getDimensionPixelOffset(R.dimen.spacing_small)
                    } else if (position == parent.adapter!!.itemCount - 1) {
                        outRect.bottom += resources.getDimensionPixelOffset(R.dimen.spacing_small)
                    }
                }
            })
        }
    }

    private fun loadLicenses() = assets.open("licenses/index.json").reader().use { reader ->
        Gson().fromJson<Map<String, License>>(reader, object : TypeToken<Map<String, License>>() {}.type)
    }

    private class LicensesRecyclerView(
        private val licenses: List<License>,
        private val layoutInflater: LayoutInflater,
        private val licenseAssets: CachedLicenseAssets
    ) : RecyclerView.Adapter<LicenseViewHolder>() {

        override fun getItemCount() = licenses.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
            val view = layoutInflater.inflate(R.layout.item_license, parent, false)
            return LicenseViewHolder(view)
        }

        override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
            val license = licenses[position]
            holder.bind(license, licenseAssets)
        }
    }

    private class LicenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView = itemView.findViewById(R.id.view_title) as TextView
        private val detailsView = itemView.findViewById(R.id.view_details) as TextView
        private val urlButton = itemView.findViewById(R.id.view_button_license) as View
        private val websiteButton = itemView.findViewById(R.id.view_button_website) as View

        fun bind(license: License, licenseAssets: CachedLicenseAssets) {
            titleView.text = license.name
            detailsView.text = license.getCopyright(licenseAssets)

            urlButton.setOnClickListener {
                openUrl(itemView.context, Uri.parse(license.licenseUrl))
            }
            websiteButton.setOnClickListener {
                openUrl(itemView.context, Uri.parse(license.url))
            }
        }

        private fun openUrl(context: Context, url: Uri) {
            val browserIntent = CustomTabsIntent.Builder().apply {
                applyAppTheme()
                setShowTitle(true)
                setExitAnimations(
                    context,
                    UiConstants.ANIMATION.ANIMATION_IN_SLIDE,
                    UiConstants.ANIMATION.ANIMATION_OUT_SLIDE
                )
            }.build().intent.apply {
                data = url
                fallbackCustomTab(context.packageManager)
            }
            context.safelyStartBrowserActivity(browserIntent)
        }
    }

    private class CachedLicenseAssets(private val assets: AssetManager) {
        private val assetCache = mutableMapOf<String, String>()
        operator fun get(fileName: String) =
            assetCache.getOrPut(fileName) { assets.readString("licenses/$fileName") }
    }

    private class License(
        @SerializedName("name")
        val name: String,
        @SerializedName("copyright")
        private val copyright: String?,
        @SerializedName("copyrightAsset")
        private val copyRightAsset: String?,
        @SerializedName("copyrightTemplate")
        private val copyrightTemplate: LicenseTemplate?,
        @SerializedName("url")
        val url: String,
        @SerializedName("license")
        val licenseUrl: String
    ) {
        fun getCopyright(licenseAssets: CachedLicenseAssets) =
            copyright
                ?: copyrightTemplate?.let {
                    val licenseType = it.type
                    val asset = when (licenseType) {
                        "apache" -> "template_apache.txt"
                        "mit" -> "template_mit.txt"
                        "bsd3" -> "template_bsd_3_clause.txt"
                        else -> error("Unknown license $licenseType")
                    }
                    licenseAssets[asset]
                        .replaceFirst("[year]", it.year)
                        .replaceFirst("[author]", it.author)
                }
                ?: copyRightAsset?.let { licenseAssets[it] } ?: ""
    }

    private class LicenseTemplate(
        @SerializedName("year")
        val year: String,
        @SerializedName("author")
        val author: String,
        @SerializedName("type")
        val type: String
    )
}
