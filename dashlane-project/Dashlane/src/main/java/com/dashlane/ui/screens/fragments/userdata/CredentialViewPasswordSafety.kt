package com.dashlane.ui.screens.fragments.userdata

import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.passwordstrength.PasswordStrengthCache
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.getPasswordStrengthScore
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.passwordstrength.textColorRes
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.getStringFormatted
import com.dashlane.util.toForegroundColorSpan
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.util.SecurityBreachUtil.isCompromised
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CredentialViewPasswordSafety(
    rootView: View,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val passwordStrengthCache: PasswordStrengthCache,
    private val vaultDataQuery: VaultDataQuery,
    private val similarPassword: SimilarPassword,
    private val coroutineScope: CoroutineScope
) {

    private val labelView = rootView.findViewById<View>(R.id.password_safety_label_textview)
    private val strengthTextView = rootView.findViewById<TextView>(R.id.password_safety_strength_textview)
    private val reusedTextView = rootView.findViewById<TextView>(R.id.password_safety_reused_textview)
    private val compromisedTextView = rootView.findViewById<TextView>(R.id.password_safety_compromised_textview)

    private var lastPasswordResult: PasswordResults? = null
    private var uiRefreshJobs: List<Job>? = null

    fun onViewMode() {
        setGlobalVisibility(View.VISIBLE)
    }

    fun onEditMode() {
        setGlobalVisibility(View.GONE)
    }

    fun setPassword(password: String?) {
        if (lastPasswordResult?.password == password) return 

        
        cancelGetDataJobs()

        lastPasswordResult = if (password == null || password.isEmpty()) {
            null
        } else {
            PasswordResults(
                password = password,
                deferredCompromised = coroutineScope.async { isCompromised(password) },
                deferredReused = coroutineScope.async { getReusedCount(password) },
                deferredStrengthScore = getPasswordStrengthScoreAsync(password)
            )
        }

        refreshUI()
    }

    private fun getPasswordStrengthScoreAsync(password: String): Deferred<PasswordStrengthScore?> {
        
        val passwordStrengthScore = passwordStrengthCache.scores[password]
        return if (passwordStrengthScore == null) {
            coroutineScope.async { runCatching { passwordStrengthEvaluator.getPasswordStrengthScore(password) }.getOrNull() }
        } else {
            
            CompletableDeferred(passwordStrengthScore)
        }
    }

    private fun cancelGetDataJobs() {
        lastPasswordResult?.apply {
            deferredCompromised.cancel()
            deferredReused.cancel()
            deferredStrengthScore.cancel()
        }
        lastPasswordResult = null
    }

    private fun cancelUiRefreshJobs() {
        uiRefreshJobs?.forEach { it.cancel() }
        uiRefreshJobs = null
    }

    private fun refreshUI() {
        cancelUiRefreshJobs()

        val passwordResults = lastPasswordResult

        if (labelView.visibility == View.VISIBLE && passwordResults != null) {
            
            startRefreshDataForUi(passwordResults)
        } else {
            
            labelView.visibility = View.GONE
            strengthTextView.visibility = View.GONE
            reusedTextView.visibility = View.GONE
            compromisedTextView.visibility = View.GONE
        }
    }

    private fun startRefreshDataForUi(passwordResults: PasswordResults) {
        val jobsPending = mutableListOf<Job>()

        
        refreshValue(
            jobsPending,
            passwordResults.deferredStrengthScore,
            onPreExecute = { strengthTextView.visibility = View.INVISIBLE },
            onPostExecute = {
                if (it == null) {
                    strengthTextView.visibility = View.GONE
                } else {
                    val context = strengthTextView.context
                    val strengthLabel = it.getShortTitle(context).toForegroundColorSpan(context.getColor(it.textColorRes))
                    strengthTextView.visibility = View.VISIBLE
                    strengthTextView.text = context.getStringFormatted(R.string.password_safety_strength, strengthLabel)
                }
            }
        )

        
        refreshValue(
            jobsPending,
            passwordResults.deferredReused,
            onPreExecute = { reusedTextView.visibility = View.INVISIBLE },
            onPostExecute = {
                if (it >= 2) {
                    reusedTextView.visibility = View.VISIBLE
                    reusedTextView.text = reusedTextView.resources.getString(R.string.password_safety_reused, it)
                } else {
                    reusedTextView.visibility = View.GONE
                }
            }
        )

        
        refreshValue(
            jobsPending,
            passwordResults.deferredCompromised,
            onPreExecute = { compromisedTextView.visibility = View.INVISIBLE },
            onPostExecute = { compromised ->
                if (compromised) {
                    compromisedTextView.setText(R.string.password_safety_compromised)
                    compromisedTextView.visibility = View.VISIBLE
                } else {
                    compromisedTextView.visibility = View.GONE
                }
            }
        )

        uiRefreshJobs = jobsPending.toList()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun <T> refreshValue(
        jobsPending: MutableList<Job>,
        deferredValue: Deferred<T>,
        onPreExecute: () -> Unit,
        onPostExecute: (T) -> Unit
    ) {
        if (deferredValue.isCompleted) {
            val value = deferredValue.getCompleted()
            onPostExecute.invoke(value)
        } else {
            val refreshUiJob = coroutineScope.launch(Dispatchers.Main) {
                onPreExecute.invoke()
                val value = deferredValue.await()
                onPostExecute.invoke(value)
            }
            jobsPending.add(refreshUiJob)
        }
    }

    private fun setGlobalVisibility(visibility: Int) {
        if (labelView.visibility == visibility) return 

        labelView.visibility = visibility
        refreshUI()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getReusedCount(password: String) = withContext(Dispatchers.Default) {
        val filter = vaultFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) }
        vaultDataQuery.queryAll(filter)
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .mapNotNull { it.syncObject.password?.toString() }
            .count { similarPassword.areSimilar(password, it) }
    }

    private suspend fun isCompromised(password: String) = withContext(Dispatchers.Default) {
        val filter = vaultFilter { specificDataType(SyncObjectType.SECURITY_BREACH) }
        vaultDataQuery.queryAll(filter).isCompromised(similarPassword, password)
    }

    companion object {
        fun newInstance(rootView: View, scope: CoroutineScope): CredentialViewPasswordSafety {
            val strengthEvaluator = SingletonProvider.getPasswordStrengthEvaluator()
            val strengthCache = SingletonProvider.getPasswordStrengthCache()
            val valutDataQuery = SingletonProvider.getMainDataAccessor().getVaultDataQuery()
            return CredentialViewPasswordSafety(
                rootView,
                strengthEvaluator,
                strengthCache,
                valutDataQuery,
                SimilarPassword(),
                scope
            )
        }
    }

    class PasswordResults(
        val password: String,
        val deferredStrengthScore: Deferred<PasswordStrengthScore?>,
        val deferredReused: Deferred<Int>,
        val deferredCompromised: Deferred<Boolean>
    )
}