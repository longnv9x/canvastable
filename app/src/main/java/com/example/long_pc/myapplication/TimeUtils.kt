package com.example.long_pc.myapplication

import android.text.format.Time
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    val TIME_FORMAT = "HH:mm"
    val ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    val ISO_8601_DATE_TIME_FORMAT_RECEIVE = "yyyy-MM-dd'T'HH:mm:ssZZ"
    val ISO_8601_DATE_TIME_FORMAT_SEND = "yyyy-MM-dd'T'HH:mm:ssZZ"
    val SUNDAY = 0
    val MONDAY = 1
    private val cacheDateFormat: HashMap<String, SimpleDateFormat> by lazy {
        HashMap<String, SimpleDateFormat>()
    }
    fun getDateFormat(pattern: String = ISO_8601_DATE_TIME_FORMAT): SimpleDateFormat {
        if (cacheDateFormat[pattern] == null) {
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            cacheDateFormat[pattern] = format
        }

        return cacheDateFormat[pattern]!!
    }
    fun today(): Calendar {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return today
    }

    @JvmOverloads
    fun daysBetween(c1: Calendar, c2: Calendar, shouldPlusOneDay: Boolean = true): Int {
        c1.set(Calendar.MILLISECOND, 0)
        c2.set(Calendar.MILLISECOND, 0)
        val startTime = Time()
        startTime.set(c1.timeInMillis)
        // The first millisecond of the next day is still the same day.
        val endTime = Time()
        endTime.set(c2.timeInMillis - 1)
        return (if (shouldPlusOneDay) 1 else 0) + Time.getJulianDay(endTime.toMillis(true), endTime.gmtoff) - Time.getJulianDay(startTime.toMillis(true), startTime.gmtoff)
    }
    fun toWeekViewPeriodIndex(instance: Calendar): Double {
        return (instance.get(Calendar.YEAR) * 12).toDouble() + instance.get(Calendar.MONTH).toDouble() + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0
    }
    fun getFirstDayOfMonth(currentTime: Date?): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        return calendar
    }
}
fun Calendar.isTheSameDay(calendar: Calendar): Boolean {
    return this.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            && this.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
}
