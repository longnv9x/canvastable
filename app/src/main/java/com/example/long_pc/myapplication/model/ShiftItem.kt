package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class ShiftItem() : Parcelable {
    var officeUserId: String? = null
    var date: Date? = null
    var morning: Session? = null
    var afternoon: Session? = null
    var night: Session? = null

    constructor(parcel: Parcel) : this() {
        morning = parcel.readParcelable(Session::class.java.classLoader)
        afternoon = parcel.readParcelable(Session::class.java.classLoader)
        night = parcel.readParcelable(Session::class.java.classLoader)
        officeUserId = parcel.readString()
        date = parcel.readValue(Date::class.java.classLoader) as Date?
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(morning, flags)
        parcel.writeParcelable(afternoon, flags)
        parcel.writeParcelable(night, flags)
        parcel.writeString(officeUserId)
        parcel.writeValue(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShiftItem> {
        override fun createFromParcel(parcel: Parcel): ShiftItem {
            return ShiftItem(parcel)
        }

        override fun newArray(size: Int): Array<ShiftItem?> {
            return arrayOfNulls(size)
        }
    }
}