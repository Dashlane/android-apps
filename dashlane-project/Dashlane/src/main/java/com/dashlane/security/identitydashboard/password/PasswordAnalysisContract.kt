package com.dashlane.security.identitydashboard.password

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.definition.Base

interface PasswordAnalysisContract {

    interface ViewProxy : Base.IView {
        fun setItems(
            itemsPerMode: Map<Mode, List<Any>>,
            modeToSelect: Mode?,
            indexToHighlight: Int?
        )

        fun setSecurityScore(score: Float)
        fun setRefreshMode(enable: Boolean)
        fun removeListenerPage()
    }

    interface Presenter : Base.IPresenter {
        fun onViewVisible()
        fun onViewHidden()
        fun onListItemClick(item: Any)
        fun setSensitiveAccountOnly(enable: Boolean)
        fun requireRefresh(forceUpdate: Boolean)
        fun onPageSelected(mode: Mode)
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun getAuthentifiantsSecurityInfo(): AuthentifiantSecurityEvaluator.Result?
        suspend fun saveModified(authentifiant: VaultItem<SyncObject.Authentifiant>)

        fun shouldDisplayProcessDuration(): Boolean
    }

    enum class Mode { COMPROMISED, REUSED, WEAK, EXCLUDED }
}