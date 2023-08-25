package com.dashlane.security.identitydashboard.item

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.security.identitydashboard.PasswordAnalysisScoreViewProxy
import com.dashlane.security.identitydashboard.password.AuthentifiantSecurityEvaluator
import com.dashlane.security.identitydashboard.password.GroupOfAuthentifiant
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IdentityDashboardPasswordHealthItem(
    var futureSecurityScoreResult: Deferred<AuthentifiantSecurityEvaluator.Result?>?,
    var listener: PasswordHeathClickListener
) : IdentityDashboardItem {

    lateinit var coroutineScope: CoroutineScope

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<IdentityDashboardPasswordHealthItem>(v) {
        private val scoreViewProxy =
            PasswordAnalysisScoreViewProxy(v.findViewById(R.id.password_health_security_score))

        private val safeCount = CountViewProxy(v.findViewById(R.id.safe))
        private val compromisedCount = CountViewProxy(v.findViewById(R.id.compromised))
        private val reusedCount = CountViewProxy(v.findViewById(R.id.reused))
        private val weakCount = CountViewProxy(v.findViewById(R.id.weak))

        private var job: Job? = null

        init {
            
            v.findViewById<View>(R.id.password_health_security_score)
                .setOnClickListener { v.performClick() }
            v.findViewById<View>(R.id.explore_button).setOnClickListener { v.performClick() }
            safeCount.init(
                R.string.security_dashboard_list_safe,
                R.color.text_positive_quiet
            ) {
                v.performClick()
            }
            
            compromisedCount.init(
                R.string.security_dashboard_list_compromised,
                R.color.text_danger_quiet
            ) {
                `object`?.listener?.onClickCompromised()
            }
            reusedCount.init(
                R.string.security_dashboard_list_reused,
                R.color.text_warning_quiet
            ) {
                `object`?.listener?.onClickReused()
            }
            weakCount.init(
                R.string.security_dashboard_list_weak,
                R.color.text_warning_quiet
            ) {
                `object`?.listener?.onClickWeak()
            }
        }

        override fun updateView(context: Context, item: IdentityDashboardPasswordHealthItem?) {
            item ?: return

            job?.cancel()

            job = item.coroutineScope.launch(Dispatchers.Main) {
                
                initSecurityScore()

                
                computeSecurityScore(item)
            }
        }

        private suspend fun computeSecurityScore(item: IdentityDashboardPasswordHealthItem) {
            val result = item.futureSecurityScoreResult?.await() ?: return

            
            val securityScore = result.securityScore
            scoreViewProxy.showProgress(securityScore)
            scoreViewProxy.setLabel(context.getString(R.string.password_health_score_label))

            
            val (compromisedCount, similarCount, weakCount) = withContext(Dispatchers.Default) {
                coroutineScope {
                    val compromisedCountAsync =
                        countAuthentifiantsAsync(result.authentifiantsByBreach)
                    val similarCountAsync =
                        countAuthentifiantsAsync(result.authentifiantsBySimilarity)
                    val weakCountAsync = countAuthentifiantsAsync(result.authentifiantsByStrength)
                    Triple(
                        compromisedCountAsync.await(),
                        similarCountAsync.await(),
                        weakCountAsync.await()
                    )
                }
            }

            
            setCounter(
                safeCount = result.totalSafeCredentials,
                compromisedCount = compromisedCount,
                similarCount = similarCount,
                weakCount = weakCount
            )
        }

        private fun setCounter(
            safeCount: Int,
            compromisedCount: Int,
            similarCount: Int,
            weakCount: Int
        ) {
            setCounterValue(this.safeCount, safeCount)
            setCounterValue(this.compromisedCount, compromisedCount)
            setCounterValue(reusedCount, similarCount)
            setCounterValue(this.weakCount, weakCount)
        }

        private fun setCounterValue(view: CountViewProxy, count: Int) {
            view.count.text = count.takeIf { count >= 0 }?.toString() ?: LOADING_PLACEHOLDER_COUNTER
        }

        private fun countAuthentifiantsAsync(authentifiantsByMode: List<GroupOfAuthentifiant<*>>): Deferred<Int> =
            CoroutineScope(Dispatchers.Default).async {
                authentifiantsByMode.flatMap { it.authentifiants }.toSet().count()
            }

        private fun initSecurityScore() {
            scoreViewProxy.showIndeterminate()

            setCounter(
                safeCount = -1,
                compromisedCount = -1,
                similarCount = -1,
                weakCount = -1
            )
        }
    }

    interface PasswordHeathClickListener {
        fun onClickCompromised()
        fun onClickReused()
        fun onClickWeak()
    }

    private class CountViewProxy(private val root: View) {
        val count: TextView = root.findViewById(R.id.count)
        val label: TextView = root.findViewById(R.id.label)

        fun init(
            @StringRes labelResId: Int,
            @ColorRes colorResId: Int,
            onClick: View.OnClickListener
        ) {
            root.setOnClickListener(onClick)
            label.setText(labelResId)
            count.setTextColor(root.context.getColor(colorResId))
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<IdentityDashboardPasswordHealthItem>(
            R.layout.item_id_password_health,
            ViewHolder::class.java
        )

        private const val LOADING_PLACEHOLDER_COUNTER = "-"
    }
}