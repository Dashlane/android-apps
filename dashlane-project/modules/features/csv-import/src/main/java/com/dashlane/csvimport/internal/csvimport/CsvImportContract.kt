package com.dashlane.csvimport.internal.csvimport

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.dashlane.csvimport.internal.CsvSchema
import com.skocken.presentation.definition.Base
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.channels.ReceiveChannel

internal interface CsvImportContract {
    interface ViewProxy : Base.IView {
        fun showLoading()
        fun showEmptyState()
        fun showList(items: List<CsvAuthentifiant>)
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)
        fun onCustomCsvImportActivityResult(resultCode: Int, data: Intent?)

        fun startMatchingFields(fields: List<String>)

        fun onPrimaryCtaClicked()
        fun onSecondaryCtaClicked()

        fun toggleAuthentifiantSelection(position: Int)
    }

    interface DataProvider : Base.IDataProvider {
        val currentState: State

        val states: ReceiveChannel<State>

        fun matchFields(types: List<CsvSchema.FieldType?>)

        fun loadAuthentifiants()

        fun saveAuthentifiants()

        fun toggleAuthentifiantSelection(position: Int)
    }

    sealed class State : Parcelable {
        @Parcelize
        object Initial : State()

        @Parcelize
        data class Matching(val fields: List<String>, val separator: Char) : State()

        @Parcelize
        object Loading : State()

        @Parcelize
        data class Loaded(
            val authentifiants: List<CsvAuthentifiant>,
            val saving: Boolean
        ) : State()

        @Parcelize
        object LoadingError : State()

        @Parcelize
        data class Saved(val count: Int) : State()
    }
}