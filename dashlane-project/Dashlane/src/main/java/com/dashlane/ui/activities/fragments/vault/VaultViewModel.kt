package com.dashlane.ui.activities.fragments.vault

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.home.vaultlist.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.withContext

class VaultViewModel(
    private val genericDataQuery: GenericDataQuery
) : ViewModel() {

    private val liveData: MutableLiveData<List<SummaryObject>> = MutableLiveData()

    @Suppress("EXPERIMENTAL_API_USAGE")
    val actor = viewModelScope.actor<Unit>(Dispatchers.Main, capacity = Channel.CONFLATED) {
        consumeEach {
            liveData.value = queryData()
        }
    }

    fun refresh() {
        actor.trySend(Unit)
    }

    fun observer(lifecycleOwner: LifecycleOwner, onChanged: Observer<List<SummaryObject>>) {
        liveData.observe(lifecycleOwner, onChanged)
    }

    private suspend fun queryData() = withContext(Dispatchers.Default) {
        val genericFilter = genericFilter {
            specificDataType(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
            forCurrentSpace()
        }
        genericDataQuery.queryAll(genericFilter)
    }
}

@Suppress("UNCHECKED_CAST")
class VaultViewModelFactory(val genericDataQuery: GenericDataQuery) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VaultViewModel(genericDataQuery) as T
    }
}
