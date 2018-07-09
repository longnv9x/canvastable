package com.example.long_pc.myapplication

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class CalendarHeaderView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val MONDAY: String = context!!.getString(R.string.COMMON_MON)
    private val TUEDAY: String = context!!.getString(R.string.COMMON_TUE)
    private val WEDNESDAY: String = context!!.getString(R.string.COMMON_WED)
    private val THURSDAY: String = context!!.getString(R.string.COMMON_THU)
    private val FRIDAY: String = context!!.getString(R.string.COMMON_FRI)
    private val SATURDAY: String = context!!.getString(R.string.COMMON_SAT)
    private val SUNDAY: String = context!!.getString(R.string.COMMON_SUN)

    private val COLOR_SATURDAY: Int = ContextCompat.getColor(context!!, R.color.saturday_number)
    private val COLOR_SUNDAY: Int = ContextCompat.getColor(context!!, R.color.sunday_number)
    private val COLOR_NORMAL: Int = ContextCompat.getColor(context!!, R.color.common_text_primary)

    private fun setMondayAsFirstDayOfWeek() {
        for (index in 0 until childCount) {
            val tvDayOfWeek = getChildAt(index) as TextView
            tvDayOfWeek.setTextColor(COLOR_NORMAL)

            when (index) {
                0 -> {
                    tvDayOfWeek.text = MONDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                1 -> {
                    tvDayOfWeek.text = TUEDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                2 -> {
                    tvDayOfWeek.text = WEDNESDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                3 -> {
                    tvDayOfWeek.text = THURSDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                4 -> {
                    tvDayOfWeek.text = FRIDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                5 -> {
                    tvDayOfWeek.text = SATURDAY
                    tvDayOfWeek.setTextColor(COLOR_SATURDAY)
                }
                6 -> {
                    tvDayOfWeek.text = SUNDAY
                    tvDayOfWeek.setTextColor(COLOR_SUNDAY)
                }

            }
        }
    }

    private fun setSundayAsFirstDayOfWeek() {
        for (index in 0 until childCount) {
            val tvDayOfWeek = getChildAt(index) as TextView

            when (index) {
                0 -> {
                    tvDayOfWeek.text = SUNDAY
                    tvDayOfWeek.setTextColor(COLOR_SUNDAY)
                }
                1 -> {
                    tvDayOfWeek.text = MONDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                2 -> {
                    tvDayOfWeek.text = TUEDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                3 -> {
                    tvDayOfWeek.text = WEDNESDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                4 -> {
                    tvDayOfWeek.text = THURSDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                5 -> {
                    tvDayOfWeek.text = FRIDAY
                    tvDayOfWeek.setTextColor(COLOR_NORMAL)
                }
                6 -> {
                    tvDayOfWeek.text = SATURDAY
                    tvDayOfWeek.setTextColor(COLOR_SATURDAY)
                }

            }
        }
    }

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        if (firstDayOfWeek == TimeUtils.MONDAY) {
            setMondayAsFirstDayOfWeek()
        } else {
            setSundayAsFirstDayOfWeek()
        }
    }
}