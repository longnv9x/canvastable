package com.example.long_pc.myapplication

import com.example.long_pc.myapplication.model.ShiftData
import java.util.*


/**
 * AP4002 カレンダー(週表示)
 */

interface CalendarWeekView  {

    fun changeSetting(fontSize: Float, firstDayOfWeek: Int)

    fun showEvents()

    fun getEventFromCache( startTime: Date, endTime: Date): ShiftData?
}
