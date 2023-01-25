package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CounterFilter



interface DataCounter {
    

    fun count(filter: CounterFilter): Int

    companion object {
        const val NO_COUNT = -1
    }
}