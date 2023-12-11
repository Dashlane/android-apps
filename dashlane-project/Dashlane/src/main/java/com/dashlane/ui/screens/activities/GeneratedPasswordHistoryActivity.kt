package com.dashlane.ui.screens.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.colorpassword.CharacterColor
import com.dashlane.util.setCurrentPageView
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeneratedPasswordHistoryActivity : DashlaneActivity() {

    @Inject
    lateinit var vaultItemLogger: VaultItemLogger

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    @Inject
    lateinit var mainDataAccessor: MainDataAccessor

    private val revealedIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated_password_history)

        setCurrentPageView(AnyPage.TOOLS_PASSWORD_GENERATOR_HISTORY)

        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.action_bar_password_generated)
        }

        savedInstanceState?.getStringArray(REVEALED_IDS)
            ?.let { revealedIds.addAll(it) }

        if (!applicationLocked) {
            refreshContent()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray(REVEALED_IDS, revealedIds.toTypedArray())
    }

    override fun onApplicationUnlocked() {
        super.onApplicationUnlocked()
        refreshContent()
    }

    private fun refreshContent() {
        val generatedPasswords = mainDataAccessor
            .getGeneratedPasswordQuery()
            .queryAllNotRevoked()
            .sortedByDescending { it.syncObject.generatedDate }

        (findViewById<RecyclerView>(R.id.view_list)).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GeneratedPasswordAdapter(generatedPasswords)
        }
    }

    private inner class GeneratedPasswordAdapter(
        private val generatedPasswords: List<VaultItem<SyncObject.GeneratedPassword>>
    ) : RecyclerView.Adapter<GeneratedPasswordViewHolder>() {

        override fun getItemCount() = generatedPasswords.size

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GeneratedPasswordViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.generated_password_listitem, parent, false)
            return GeneratedPasswordViewHolder(view)
        }

        override fun onBindViewHolder(holder: GeneratedPasswordViewHolder, position: Int) {
            val password = generatedPasswords[position]
            holder.bind(password)
        }
    }

    private inner class GeneratedPasswordViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById(R.id.title) as TextView
        private val passwordLayout = itemView.findViewById(R.id.password_layout) as TextInputLayout
        private val password = itemView.findViewById(R.id.password) as TextInputEditText
        private val copyPassword = itemView.findViewById(R.id.copy_password) as ImageButton

        private var authDomain: String? = null
        private var generatedDate: Long? = null
        private var generatedId: String? = null

        private val characterColor: CharacterColor = CharacterColor(itemView.context)

        init {
            passwordLayout.addOnFieldVisibilityToggleListener { visible ->
                if (visible) {
                    generatedId?.let {
                        vaultItemLogger.logRevealField(
                            Field.PASSWORD,
                            it,
                            ItemType.GENERATED_PASSWORD,
                            authDomain
                        )
                    }
                }

                generatedId?.let {
                    if (visible) {
                        revealedIds += it
                    } else {
                        revealedIds -= it
                    }
                }
            }
        }

        fun bind(generatedPasswordVaultItem: VaultItem<SyncObject.GeneratedPassword>) {
            val generatedPassword = generatedPasswordVaultItem.syncObject
            generatedId = generatedPasswordVaultItem.uid

            val domain = generatedPassword.domain?.toUrlDomainOrNull()?.value
            authDomain = domain
            generatedDate = generatedPassword.generatedDate
            val formattedDate = generatedDate?.let {
                DateUtils.formatDateTime(
                    itemView.context,
                    it * 1000,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
                )
            }

            val titleText = when {
                formattedDate != null && domain != null && !generatedPassword.authId.isNullOrBlank() ->
                    getString(R.string.generated_password_title_saved, domain, formattedDate)
                formattedDate != null && domain != null ->
                    getString(R.string.generated_password_title, domain, formattedDate)
                domain != null -> getString(R.string.generated_password_title_no_date, domain)
                formattedDate != null -> getString(R.string.generated_password_title_no_domain, formattedDate)
                else -> getString(R.string.generated_password_title_no_date_no_domain)
            }

            title.text = titleText
            password.apply {
                generatedId?.apply {
                    transformationMethod =
                        if (this in revealedIds) null else PasswordTransformationMethod()

                    val spannable = SpannableStringBuilder(generatedPassword.password).apply {
                        forEachIndexed { index, _ ->
                            characterColor.setColorForIndex(this, index)
                        }
                    }

                    setText(spannable, TextView.BufferType.EDITABLE)
                    isFocusable = false
                    isCursorVisible = false
                    keyListener = null
                }
            }
            copyPassword.setOnClickListener {
                clipboardCopy.copyToClipboard(data = password.text?.toString().orEmpty(), sensitiveData = true)
                vaultItemLogger.logCopyField(
                    field = Field.PASSWORD,
                    itemId = generatedPasswordVaultItem.uid,
                    itemType = ItemType.GENERATED_PASSWORD,
                    isProtected = false,
                    domain = generatedPassword.domain
                )
            }
        }
    }

    companion object {
        private const val REVEALED_IDS = "revealed_ids"
    }
}