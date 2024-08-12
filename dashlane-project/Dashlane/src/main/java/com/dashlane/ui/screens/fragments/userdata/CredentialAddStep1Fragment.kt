package com.dashlane.ui.screens.fragments.userdata

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.limitations.PasswordLimitationLogger
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.loaders.InstalledAppAndPopularWebsiteLoader
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.CredentialsDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.status.DefaultStatusFilter
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordActivity.Companion.newIntent
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.HeaderItem
import com.dashlane.ui.widgets.view.ExpandableCardView
import com.dashlane.ui.widgets.view.ImportMethodItem
import com.dashlane.ui.widgets.view.Infobox
import com.dashlane.ui.widgets.view.MultiColumnRecyclerView
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.util.DeviceUtils.hideKeyboard
import com.dashlane.util.setCurrentPageView
import com.google.android.material.textfield.TextInputLayout
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

@AndroidEntryPoint
class CredentialAddStep1Fragment :
    AbstractContentFragment(),
    InstalledAppAndPopularWebsiteLoader.Listener,
    EfficientAdapter.OnItemClickListener<DashlaneRecyclerAdapter.ViewTypeProvider> {

    @Inject
    lateinit var backupCoordinator: BackupCoordinator

    @Inject
    lateinit var urlDomainRegistryFactory: UrlDomainRegistryFactory

    @Inject
    lateinit var dataCounter: DataCounter

    @Inject
    lateinit var knownApplicationProvider: KnownApplicationProvider

    @Inject
    lateinit var passwordLimiter: PasswordLimiter

    @Inject
    lateinit var passwordLimitationLogger: PasswordLimitationLogger

    private lateinit var websiteUrlInput: TextInputLayout
    private lateinit var recyclerView: MultiColumnRecyclerView
    private lateinit var popularWebsiteProgress: ProgressBar
    private lateinit var passwordLimitReachedInfobox: Infobox

    private val isFirstPassword
        get() = dataCounter.count(
                CounterFilter(
                    CredentialsDataTypeFilter,
                    NoSpaceFilter,
                    DefaultLockFilter,
                    DefaultStatusFilter
                )
            ) == 0

    private var popularWebsites: List<String> = emptyList()

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val suggestionsActor = lifecycleScope.actor<String>(capacity = Channel.CONFLATED) {
        consumeEach { query ->
            val urlDomainRegistry = urlDomainRegistryFactory.create()
            val items: List<DashlaneRecyclerAdapter.ViewTypeProvider> = when {
                query.length <= 1 -> {
                    sequence {
                        if (!isFirstPassword) {
                            yield(DomainItem(query))
                        }
                        yield(HeaderItem(getString(R.string.fragment_credential_create_step1_popular_websites)))
                        yieldAll(popularWebsites.map { DomainItem(it) })
                    }.toList()
                }
                else -> {
                    val domains = urlDomainRegistry.search(query).map { it.value }
                    sequence {
                        yield(DomainItem(query))
                        yield(HeaderItem(getString(R.string.fragment_credential_create_step1_suggestions)))
                        yieldAll(domains.map { DomainItem(it) })
                    }.toList()
                }
            }
            recyclerView.adapter?.populateItems(items)
            hideLoader()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setCurrentPageView(AnyPage.HOME_ADD_ITEM)
        val view = inflater.inflate(R.layout.fragment_create_credential_step1, container, false)
        websiteUrlInput = view.findViewById(R.id.website_url_input_layout)
        recyclerView = view.findViewById(R.id.recyclerView)
        popularWebsiteProgress = view.findViewById(R.id.popular_website_gridview_loader)
        passwordLimitReachedInfobox = view.findViewById(R.id.infobox_password_limit_reached)
        websiteUrlInput.editText?.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        val expandableCardView = view.findViewById<ExpandableCardView>(R.id.expandableImportMethods)

        if (savedInstanceState == null) {
            val args = CredentialAddStep1FragmentArgs.fromBundle(requireArguments())
            expandableCardView.setExpanded(args.expandImportOptions, false)
        }

        
        
        @Suppress("MissingInflatedId")
        view.findViewById<ImportMethodItem>(R.id.csv).apply {
            setOnClickListener {
                if (passwordLimiter.hasPasswordLimit) {
                    navigator.goToOffers()
                } else {
                    navigator.goToCsvImportIntro()
                }
            }
            setBadgeVisibility(passwordLimiter.hasPasswordLimit)
        }

        @Suppress("MissingInflatedId")
        view.findViewById<View>(R.id.computer).setOnClickListener {
            navigator.goToM2wImportIntro()
        }

        @Suppress("MissingInflatedId")
        view.findViewById<View>(R.id.backup).setOnClickListener {
            backupCoordinator.startImport()
        }

        @Suppress("MissingInflatedId")
        view.findViewById<ImportMethodItem>(R.id.password_manager).apply {
            setOnClickListener {
                if (passwordLimiter.hasPasswordLimit) {
                    navigator.goToOffers()
                } else {
                    navigator.goToCompetitorImportIntro()
                }
            }
            setBadgeVisibility(passwordLimiter.hasPasswordLimit)
        }

        recyclerView.adapter?.onItemClickListener = this

        setHasOptionsMenu(true)
        websiteUrlInput.editText?.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE && v.text.isNotEmpty()) {
                goToNextStep(true, null)
            }
            false
        }
        websiteUrlInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                
            }

            override fun afterTextChanged(s: Editable) {
                suggestionsActor.trySend(s.toString())
            }
        })
        return view
    }

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (passwordLimiter.isPasswordLimitReached()) {
            
            
            popularWebsiteProgress.visibility = View.GONE
            recyclerView.visibility = View.GONE
            websiteUrlInput.visibility = View.GONE
            passwordLimitReachedInfobox.visibility = View.VISIBLE
            passwordLimitReachedInfobox.primaryButton.setOnClickListener {
                passwordLimitationLogger.upgradeFromBanner()
                navigator.goToOffers()
            }
        } else {
            showLoader()
            InstalledAppAndPopularWebsiteLoader(this, this, knownApplicationProvider).start()
        }
    }

    private fun goToNextStep(getUrlFromInput: Boolean, url: String?) {
        var userURL = url
        if (getUrlFromInput) {
            userURL = websiteUrlInput.editText?.text.toString()
        }
        openNextStep(userURL)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard(view)
    }

    override fun onLoadFinished(result: List<String>?) {
        this.popularWebsites = result ?: emptyList()
        suggestionsActor.trySend(websiteUrlInput.editText?.text?.toString().orEmpty())
    }

    override fun onItemClick(
        adapter: EfficientAdapter<DashlaneRecyclerAdapter.ViewTypeProvider>,
        view: View,
        item: DashlaneRecyclerAdapter.ViewTypeProvider?,
        position: Int
    ) {
        when (item) {
            is DomainItem -> goToNextStep(false, item.domain)
        }
    }

    private fun openNextStep(url: String?) {
        if (isFirstPassword) {
            val activity = requireActivity()
            val intent = newIntent(activity, url!!)
            activity.startActivity(intent)
            activity.onBackPressed()
            return
        }

        activity?.onBackPressed()
        navigator.goToCreateAuthentifiant(url ?: "")
    }

    private fun showLoader() {
        popularWebsiteProgress.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        popularWebsiteProgress.visibility = View.GONE
    }
}