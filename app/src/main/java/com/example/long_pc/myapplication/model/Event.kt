package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.example.long_pc.myapplication.ConstantCalendar
import com.example.long_pc.myapplication.DJCalendarEnum
import com.example.long_pc.myapplication.isTheSameDay
import java.util.*

open class Event constructor() : Parcelable {

    companion object CREATOR : Parcelable.Creator<Event> {
        private val calendarConverter: Calendar = Calendar.getInstance()
        private val TIME_PLUS_ALLDAY = 12
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

    constructor(dest: Parcel?) : this() {
        calendarType = DJCalendarEnum.Type.valueOf(dest?.readString()!!)
        id = dest.readString()
    }

    /**
     * イベントID
     */
    var id: String? = null

    /**
     * カレンダーID
     */
    var calendarId: String? = null

    /**
     * カレンダー種別
     */
    var calendarType: DJCalendarEnum.Type? = null

    /**
     * イベントタイトル
     */
    var title: String? = null

    /**
     * 開始時間
     */
    var start: Date = Date()
        set(value) {
            field = value
            startCalendar.time = field
        }

    /**
     * 終了時間
     */
    var end: Date = Date()
        set(value) {
            field = value
            endCalendar.time = field
        }

    /**
     * 終日フラグ
     */
    var allDay: Boolean = false

    /**
     * 繰り返しルール
     * 繰り返しでない場合は空
     */
    var repeatRule: String? = null

    var repeatDescription: String? = null
    /**
     * Visible in calendar
     */
    var visible: Boolean = true

    var startCalendar: Calendar = Calendar.getInstance()

    var endCalendar: Calendar = Calendar.getInstance()

    /**
     * 公開対象
     * スタッフID or グループID
     * どちらのIDかはカレンダー種別を参照する
     */
    var targetId: String? = null

    override fun equals(other: Any?): Boolean {
        if (other is Event) {
            if (id != other.id) return false
            if (calendarId != other.calendarId) return false
            if (calendarType?.id != other.calendarType?.id) return false
            if (title != other.title) return false
            if (start.time != other.start.time) return false
            if (end.time != other.end.time) return false
            if (repeatRule != other.repeatRule) return false
            if (repeatDescription != other.repeatDescription) return false
            if (allDay != other.allDay) return false
            if (targetId != other.targetId) return false
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (calendarId?.hashCode() ?: 0)
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (calendarType?.hashCode() ?: 0)
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (title?.hashCode() ?: 0)
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + start.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + end.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + allDay.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (repeatRule?.hashCode() ?: 0)
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (repeatDescription?.hashCode() ?: 0)
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + visible.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + startCalendar.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + endCalendar.hashCode()
        result = ConstantCalendar.HASH_CODE_MAGIC_NUMBER * result + (targetId?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(calendarType?.id!!.toString())
        dest?.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

}

val Event.isMultipleEvent: Boolean
    get() = start.isTheSameDay(end)