package com.dashlane.util.time



class TimeMeasurement(private val title: String) {

    private val ticker = TimeTicker()
    private val measures = mutableListOf<Pair<String, Long>>()

    

    fun tick(key: String) {
        measures.add(key to ticker.tick())
    }

    override fun toString(): String {
        return "[TimeMeasurement] $title\n" +
                measures.joinToString { "${it.first}: ${it.second}\n" }
    }
}