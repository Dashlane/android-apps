package com.dashlane.core.domain

import com.dashlane.regioninformation.geographicalstates.GeographicalStatesRepositoryImpl
import com.dashlane.xml.domain.utils.Country



data class State(
    val country: String,
    val level: Int,
    val codeStr: String,
    val name: String
) {
    val stateDescriptor: String
        get() = "$country-$level-$codeStr"

    companion object {

        private val geographicalStateRegions by lazy {
            GeographicalStatesRepositoryImpl().getGeographicalStateRegions()
        }

        fun getStatesForCountry(country: Country): List<State> =
            geographicalStateRegions.filter { it.code == country.isoCode.uppercase() }
                .flatMap { (countryCode, level, states) ->
                    states.map {
                        State(countryCode, level.toInt(), it.code, it.name)
                    }
                }
    }
}
