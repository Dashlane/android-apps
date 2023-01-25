package com.dashlane.authenticator

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.EXTRA_INPUT
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.EXTRA_SUCCESS
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.RESULT_INPUT
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsCredentialItemAdapter
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract.SuccessResultContract.Input
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class AuthenticatorMultipleMatchesResult : DashlaneActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mutliple_matches_result)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = null
        }
        val inputs = intent.getParcelableArrayListExtra<Input>(EXTRA_INPUTS)
        if (inputs == null || inputs.size < 2) {
            
            finish()
            return
        }
        val domain = inputs.first().domain ?: getString(R.string.authenticator_default_account_name)
        findViewById<TextView>(R.id.authenticator_multiple_matches_title).text =
            getString(R.string.authenticator_multiple_matches_title, inputs.size, domain)
        findViewById<RecyclerView>(R.id.authenticator_multiple_matches_list).apply {
            layoutManager = LinearLayoutManager(context)
            hasFixedSize()
            adapter = AuthenticatorSuggestionsCredentialItemAdapter().apply {
                addAll(inputs.map {
                    CredentialItem(it.itemId!!, it.itemTitle, it.domain, it.itemUsername).apply {
                        layout = R.layout.authenticator_credential_multiple_matches_item
                    }
                })
                setOnItemClickListener { _, _, _, position ->
                    val input = inputs[position]
                    
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(RESULT_INPUT, input)
                    })
                    
                    startActivity(
                        Intent(
                            this@AuthenticatorMultipleMatchesResult,
                            AuthenticatorResultIntro::class.java
                        ).apply {
                            putExtra(EXTRA_SUCCESS, true)
                            putExtra(EXTRA_INPUT, input)
                        }
                    )
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_INPUTS = "extra_inputs"
    }
}