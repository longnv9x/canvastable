package com.example.long_pc.myapplication


/**
 * AP4002 カレンダー(週表示)
 */

interface CalendarWeekView  {

    fun changeSetting(fontSize: Float, firstDayOfWeek: Int)

    fun showEvents()
}
