package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.example.long_pc.myapplication.TimeUtils

data class SettingCalendar(var blockContentsNum: Int = BLOCK_CONTENT_NUMBER_DEFAULT,
                           var fontSize: Int = FONT_SIZE_DEFAULT,
                           var previewNum: Int = PREVIEW_NUMBER_DEFAULT,
                           var startWeek: Int = START_WEEK_DEFAULT) : Parcelable {

    companion object CREATOR : Parcelable.Creator<SettingCalendar> {

        @JvmField
        val BLOCK_CONTENT_NUMBER_DEFAULT = 4

        @JvmField
        val PREVIEW_NUMBER_DEFAULT = 6

        @JvmField
        val FONT_SIZE_DEFAULT = 11 // Update follow smoke test 5/22

        @JvmField
        val START_WEEK_DEFAULT = TimeUtils.MONDAY

        override fun createFromParcel(parcel: Parcel): SettingCalendar {
            return SettingCalendar(parcel)
        }

        override fun newArray(size: Int): Array<SettingCalendar?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(blockContentsNum)
        parcel.writeInt(fontSize)
        parcel.writeInt(previewNum)
        parcel.writeInt(startWeek)
    }

    override fun describeContents(): Int {
        return 0
    }

}