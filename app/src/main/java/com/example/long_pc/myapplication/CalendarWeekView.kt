package com.example.long_pc.myapplication

import com.example.long_pc.myapplication.model.ShiftData
import com.example.long_pc.myapplication.model.ShiftItem
import java.util.*


/**
 * AP4002 カレンダー(週表示)
 */

interface CalendarWeekView  {

    fun changeSetting(fontSize: Float, firstDayOfWeek: Int)

    fun showEvents()

    fun getEventFromCache( startTime: Date, endTime: Date): ShiftData?

    fun showPlanPreviewBottomSheet(isShow: Boolean, events:ShiftItem?= null, dateSelected: Date?= null)

    fun setPreviewEventShowNumber(showNumber: Int, textSize: Int, weekNumber: Int)
}
