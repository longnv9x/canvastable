package com.example.long_pc.myapplication

import com.example.long_pc.myapplication.model.EventSummary

interface WeekViewLoader {

    /**
     * Load the events within the period
     * @param periodIndex the period to load
     * @return A list with the events of this period
     */
    fun onLoad(periodIndex: Int): List<EventSummary>
}