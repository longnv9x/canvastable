package com.example.long_pc.myapplication

import android.graphics.Color
import android.util.Log
import com.example.long_pc.myapplication.model.*
import jp.drjoy.app.domain.model.shift.Memo
import jp.drjoy.app.domain.model.shift.Shift
import java.util.*


/**
 * AP4002 カレンダー(週表示)
 */
class CalendarWeekPresenter {

    enum class LoadingStatus {
        IDLE, WAITING, COMPLETED
    }

    private var view: CalendarWeekView? = null

    /**
     * The current month which week view is displaying
     */
    var currentTime: Calendar? = Calendar.getInstance()

    /**
     * The property to iterator in processing of get list events from cache.
     * Because I don't want to create so much Calendar instance
     */
    private var calendarIterator: Calendar = Calendar.getInstance()

    /**
     * The current period to load. The value of property is created by number of year,
     * number of month and number of day(15) of the middle day of month which is loading events from api
     */
    private var fetchedPeriod = -1

    /**
     * State of week view
     * IDLE: Default state, the state when week view don't load api
     * WAITING: The state when week view is loading API
     * COMPLETED: The state when the recent API is loaded completed
     */
    private var loadingStatus = LoadingStatus.IDLE

    /**
     * The day which is selecting in Month Fragment
     */
    var currentFocusedDay: Date? = null

    var calendarSetting: SettingCalendar = SettingCalendar()
    fun attachView(view: CalendarWeekView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

//    fun getData(arguments: Bundle?) {
//        var timeInMillis = arguments?.getLong(ConstantCalendar.KEY_CURRENT_TIME)
//                ?: System.currentTimeMillis()
//        val date = Date(timeInMillis)
//        currentTime = TimeUtils.getFirstDayOfWeek(date, calendarSetting.startWeek)
//
//        timeInMillis = arguments?.getLong(ConstantCalendar.KEY_CURRENT_FOCUSED_DAY) ?: System.currentTimeMillis()
//        val hasCurrentFocusedDay = timeInMillis != 0L
//        if (hasCurrentFocusedDay) {
//            date.time = timeInMillis
//            currentFocusedDay = date
//        }
//
//        calendarSetting = arguments?.getParcelable(ConstantCalendar.KEY_SETTING) ?: calendarSetting
//    }

    /**
     * Check if need to call api to get more event
     *
     * @param periodIndex The period index in which the date falls (floating point number).
     */
    fun checkToGetEventFromApi(periodIndex: Int) {
        val day: Calendar = Calendar.getInstance()
        day.set(periodIndex / 12, periodIndex % 12, 1)

        val middleTime = day.clone() as Calendar
        middleTime.add(Calendar.DATE, 15)

        // Populate the week view with some events.
        // Return only the events that matches newYear and newMonth.
        val period = TimeUtils.toWeekViewPeriodIndex(middleTime).toInt()
        if (loadingStatus == LoadingStatus.IDLE || fetchedPeriod != period) {
            loadingStatus = LoadingStatus.WAITING
            fetchedPeriod = period
            // view?.requestApi(currentTime!!.time, day.time)
        } else if (loadingStatus == LoadingStatus.COMPLETED) {
            loadingStatus = LoadingStatus.IDLE
        }
    }

    /**
     * Get event from memory cache. The process is handled in main thread
     *
     * @param periodIndex the period to load
     * @return A list of event
     */
    fun getEventFromCache(periodIndex: Int): ShiftData? {
        val startTime: Calendar = Calendar.getInstance()
        val endTime: Calendar = Calendar.getInstance()

        val endPeriod = periodIndex + 1

        startTime.set(periodIndex / 12, periodIndex % 12, 1)
        startTime.add(Calendar.DAY_OF_MONTH, -4)

        endTime.set(endPeriod / 12, endPeriod % 12, 1)
        endTime.add(Calendar.DAY_OF_MONTH, 4)

        return view?.getEventFromCache(startTime.time, endTime.time)
    }

    fun getEventFromCache(firstDay: Calendar): ShiftData? {
        val startTime: Calendar = firstDay.clone() as Calendar
        val endTime: Calendar= firstDay.clone() as Calendar
        endTime.add(Calendar.DAY_OF_MONTH,3)
        return view?.getEventFromCache(startTime.time, endTime.time)
    }
    /**
     * Get setting to setup week view
     */

    fun checkToRefreshView(startDate: Date) {
        val isCurrentMonthVisible = startDate.month == currentTime!!.get(Calendar.MONTH)
        if (isCurrentMonthVisible) {
            view?.showEvents()
        }
    }

    fun loadCalendarSetting(setting: SettingCalendar? = null) {
        if (setting != null) {
            this.calendarSetting = setting
        }
        view?.changeSetting(this.calendarSetting.fontSize.toFloat(), this.calendarSetting.startWeek)
    }

    fun getEventFromCacheForWeek(startTime: Date, endTime: Date): ShiftData? {
        Log.e("day","startTime --${startTime.day} endTime ${endTime.day}")
        val arrShift = arrayListOf<Shift>()
        val arrMemo = arrayListOf<Memo>()
        var tempDay = startTime
        var i = 0
        while (tempDay.time <= endTime.time) {
            when {
                i % 3 == 0 -> arrMemo.add(Memo().apply {
                    this.publicNote = "publicNote"
                    this.targetDate = tempDay
                })
                i % 2 == 0 -> arrMemo.add(Memo().apply {
                    this.targetDate = tempDay
                })
                else -> arrMemo.add(Memo().apply {
                    this.targetDate = tempDay
                    this.publicNote = "publicNote"
                    this.privateNote = "privateNote"
                })
            }
            i++
            tempDay=  tempDay.getDaysAgo(1)
        }
        (0..100).forEach { pos ->
            arrShift.add(Shift().apply {
                departmentId = "departmentId$pos"
                officeUserId = "officeUserId$pos"
                userName = "userName$pos"
                teamDisplayOrder = "teamDisplayOrder"
                memberDisplayOrder = "memberDisplayOrder"
                isGuest = false
                guestDepartmentName = "guestDepartmentName"
                memberName = "Name $pos"
                teamColor = Color.GREEN
                teamId = "teamId$pos"
                teamShortName = "team"
                shiftList = arrayListOf()
                tempDay = startTime
                while (tempDay.time <= endTime.time) {
                    shiftList!!.add(ShiftItem().apply {
                        officeUserId = "officeUserId$pos"
                        date = tempDay
                        morning = Session().apply {
                            id = "id $pos"
                            departmentId = "departmentId$pos"
                            category = "category$pos"
                            shortName = "shortName$pos"
                            name = if (pos%3==0) "morning" else "afternoon"
                            color = randomColor()
                            editable = true
                        }
                        afternoon = Session().apply {
                            id = "id $pos"
                            departmentId = "departmentId$pos"
                            category = "category$pos"
                            shortName = "shortName$pos"
                            name = if (pos%2==0) "morning" else "afternoon"
                            color = randomColor()
                            editable = true
                        }
                        night = Session().apply {
                            id = "id $pos"
                            departmentId = "departmentId$pos"
                            category = "category$pos"
                            shortName = "shortName$pos"
                            name = if (pos%4==0) "morning" else "afternoon"
                            color = randomColor()
                            editable = true
                        }
                    })
                    tempDay=  tempDay.getDaysAgo(1)
                }
            })
        }

        return ShiftData().apply {
            departmentId = "departmentId"
            isPublished = true
            memos = arrMemo
            shifts = arrShift
        }
    }

    private fun randomColor(): Int {
        val str = "abcdef0123456789"
        var color = "#"

        for (i in 1..6) {
            color += str[((Math.random() * str.length).toInt())]
        }

        return Color.parseColor(color)
    }
}
