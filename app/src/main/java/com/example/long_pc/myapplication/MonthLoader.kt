package com.example.long_pc.myapplication

import com.example.long_pc.myapplication.model.ShiftData
import java.util.*

class MonthLoader(private var onMonthChangeListener: MonthChangeListener) : WeekViewLoader {
    override fun onLoad(firstDay: Calendar): ShiftData? {
       return onMonthChangeListener.onDayChange(firstDay)
    }

    override fun onLoad(periodIndex: Int): ShiftData? {
        return onMonthChangeListener.onMonthChange(periodIndex)
    }

    interface MonthChangeListener {
        /**
         * Very important interface, it's the base to load events in the calendar.
         * This method is called three times: once to load the previous month, once to load the next month and once to load the current month.<br></br>
         * **That's why you can have three times the same event at the same place if you mess up with the configuration**
         * @return a list of the events happening **during the specified month**.
         */
        fun onMonthChange(periodIndex: Int): ShiftData?
        fun onDayChange(firstDay: Calendar): ShiftData?
    }
}