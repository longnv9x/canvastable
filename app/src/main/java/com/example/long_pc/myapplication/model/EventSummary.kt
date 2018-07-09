package com.example.long_pc.myapplication.model

import com.example.long_pc.myapplication.ColorUtils
import com.example.long_pc.myapplication.isTheSameDay
import java.util.*

class EventSummary : Event() {

    companion object {
        private val TIME_PLUS_ALL_DAY = 12
        private val calendarConverter: Calendar = Calendar.getInstance()
    }
    /**
     * イベント色
     */
    var color: String = ColorUtils.DEFAULT_COLOR

    /**
     * 場所
     */
    var place: String? = null

    var mr: User? = null


    /**
     * Split a multi-day event to a list events. Eg: events 2 days -> list of 2 events
     */
    fun splitEvents(): List<EventSummary> {
        val events = ArrayList<EventSummary>()
        var endTime = this.endCalendar.clone() as Calendar

        val eventFirst: EventSummary
        val otherDay: Calendar

        if (!this.startCalendar.isTheSameDay(endTime)) {
            endTime = this.startCalendar.clone() as Calendar
            endTime.set(Calendar.HOUR_OF_DAY, 23)
            endTime.set(Calendar.MINUTE, 59)

            eventFirst = EventSummary()
            eventFirst.id = this.id
            eventFirst.title = this.title
            eventFirst.place = this.place
            eventFirst.start = this.start
            eventFirst.end = endTime.time
            eventFirst.allDay = this.allDay
            eventFirst.color = this.color
            eventFirst.calendarType = this.calendarType
            events.add(eventFirst)

            // Add other days.
            otherDay = this.startCalendar.clone() as Calendar
            otherDay.add(Calendar.DATE, 1)

            var overDay: Calendar
            var endOfOverDay: Calendar
            var eventMore: EventSummary

            while (!otherDay.isTheSameDay(this.endCalendar)) {
                overDay = otherDay.clone() as Calendar
                overDay.set(Calendar.HOUR_OF_DAY, 0)
                overDay.set(Calendar.MINUTE, 0)

                endOfOverDay = overDay.clone() as Calendar
                endOfOverDay.set(Calendar.HOUR_OF_DAY, 23)
                endOfOverDay.set(Calendar.MINUTE, 59)

                eventMore = EventSummary()
                eventMore.id = this.id
                eventMore.title = this.title
                eventMore.place = null
                eventMore.start = overDay.time
                eventMore.end = endOfOverDay.time
                eventMore.allDay = this.allDay
                eventMore.color = this.color
                eventMore.calendarType = this.calendarType
                events.add(eventMore)

                // Add next day.
                otherDay.add(Calendar.DATE, 1)
            }

            if ((this.endCalendar.timeInMillis - this.endCalendar.get(Calendar.MILLISECOND)) > this.startCalendar.timeInMillis - this.startCalendar.get(Calendar.MILLISECOND)) {
                // Add last day.
                val startTime = this.endCalendar.clone() as Calendar
                startTime.set(Calendar.HOUR_OF_DAY, 0)
                startTime.set(Calendar.MINUTE, 0)

                val eventLast = EventSummary()
                eventLast.id = this.id
                eventLast.title = this.title
                eventLast.place = this.place
                eventLast.start = startTime.time
                eventLast.end = this.end
                eventLast.allDay = this.allDay
                eventLast.color = this.color
                eventLast.calendarType = this.calendarType
                events.add(eventLast)
            }
        } else {
            events.add(this)
        }

        return events
    }
}

val EventSummary.isMultipleEvent: Boolean
    get() = !start.isTheSameDay(end)
