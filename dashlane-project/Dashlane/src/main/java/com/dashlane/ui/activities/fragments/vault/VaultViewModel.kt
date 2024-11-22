package com.dashlane.ui.activities.fragments.vault

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dashlane.feature.home.data.VaultItemsRepository
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultItemsRepository: VaultItemsRepository,
) : ViewModel() {

    fun refresh() {
        viewModelScope.launch {
            vaultItemsRepository.loadVault()
        }
    }

    fun observer(onChanged: Observer<List<SummaryObject>>) {
        viewModelScope.launch {
            vaultItemsRepository.vaultItems
                .collect(onChanged::onChanged)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class VaultViewModelFactory(
    private val vaultItemsRepository: VaultItemsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VaultViewModel(vaultItemsRepository) as T
    }
}
