package com.dashlane.ui.activities.fragments.vault

import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject


class VaultDataProvider @Inject constructor() : BaseDataProvider<Vault.Presenter>(), Vault.DataProvider