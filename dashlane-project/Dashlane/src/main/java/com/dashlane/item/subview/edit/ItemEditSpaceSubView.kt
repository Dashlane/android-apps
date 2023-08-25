package com.dashlane.item.subview.edit

import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.ValueChangeManagerImpl
import com.dashlane.teamspaces.manager.firstOrNullMatchingDefinedDomain
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.model.VaultItem

class ItemEditSpaceSubView(
    override var value: Teamspace,
    val values: List<Teamspace>,
    private var allowSuggestionAutoChange: Boolean,
    private val views: List<ItemSubView<String>>?,
    private val linkedWebsites: List<String>,
    valueUpdate: (VaultItem<*>, Teamspace) -> VaultItem<*>
) : ItemEditValueSubView<Teamspace>(valueUpdate) {

    val enableValueChangeManager = ValueChangeManagerImpl<Boolean>()
    var changeEnable: Boolean = isChangeEnable(getSuggestedTeamspace())
        private set

    enum class CategorizationMethod {
        FORCED_CATEGORIZATION,
        SMART_CATEGORIZATION,
        MANUAL
    }

    val categorizationMethod: CategorizationMethod
        get() {
            val suggestedTeamspace = getSuggestedTeamspace()
            if (suggestedTeamspace != null && suggestedTeamspace == value) {
                return if (suggestedTeamspace.isDomainRestrictionsEnable) {
                    CategorizationMethod.FORCED_CATEGORIZATION
                } else {
                    CategorizationMethod.SMART_CATEGORIZATION
                }
            }
            return CategorizationMethod.MANUAL
        }

    private val initialValue = value
    private var valueChangedAutomatically = false

    init {
        
        
        getSuggestedTeamspace()?.let {
            if (shouldAssignToSuggestedTeamspace(it)) {
                notifyValueChanged(it)
            }
        }
    }

    private val listenerViewChanges: ValueChangeManager.Listener<String> by lazy {
        object : ValueChangeManager.Listener<String> {
            override fun onValueChanged(origin: Any, newValue: String) {
                val suggestedTeamspace = getSuggestedTeamspace()

                updateChangeEnable(suggestedTeamspace)

                if (shouldAssignToSuggestedTeamspace(suggestedTeamspace)) {
                    valueChangedAutomatically = true
                    notifyValueChanged(suggestedTeamspace!!)
                    valueChangedAutomatically = false
                }
            }
        }
    }

    private fun updateChangeEnable(teamspace: Teamspace?) {
        val changeWasEnable = changeEnable
        val changeIsNowEnable = isChangeEnable(teamspace)
        if (changeWasEnable != changeIsNowEnable) {
            changeEnable = changeIsNowEnable
            enableValueChangeManager.notifyListeners(this, changeIsNowEnable)
        }
    }

    private fun isChangeEnable(teamspace: Teamspace?) =
        (teamspace == null || !teamspace.isDomainRestrictionsEnable)

    private fun getSuggestedTeamspace(): Teamspace? {
        val allViewValues = (views?.map { it.value } ?: emptyList()) + linkedWebsites

        
        return (
            values.firstOrNullMatchingDefinedDomain(allViewValues.toTypedArray())
        
            ?: initialValue.takeIf { allowSuggestionAutoChange }
        )
    }

    private fun shouldAssignToSuggestedTeamspace(suggestedTeamspace: Teamspace?) =
        suggestedTeamspace != null && suggestedTeamspace != value &&
                (allowSuggestionAutoChange || suggestedTeamspace.isDomainRestrictionsEnable)

    init {
        enableValueChangeManager.addValueChangedListener(object : ValueChangeManager.Listener<Boolean> {
            override fun onValueChanged(origin: Any, newValue: Boolean) {
                changeEnable = newValue
            }
        })
        views?.forEach {
            (it as? ItemEditValueSubView<String>)?.addValueChangedListener(listenerViewChanges)
        }
    }

    override fun onItemValueHasChanged(previousValue: Teamspace, newValue: Teamspace) {
        super.onItemValueHasChanged(previousValue, newValue)
        if (!valueChangedAutomatically) {
            
            allowSuggestionAutoChange = false
        }
    }
}