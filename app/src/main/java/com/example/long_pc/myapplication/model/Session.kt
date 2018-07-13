package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable

class Session() : Parcelable {
    var id: String? = null
    var departmentId: String? = null
    var category: String? = null
    var shortName: String? = null
    var name: String? = null
    var color: Int? = null
    var editable: Boolean? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        departmentId = parcel.readString()
        category = parcel.readString()
        shortName = parcel.readString()
        name = parcel.readString()
        color = parcel.readValue(Int::class.java.classLoader) as Int?
        editable = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(departmentId)
        parcel.writeString(category)
        parcel.writeString(shortName)
        parcel.writeString(name)
        parcel.writeValue(color)
        parcel.writeValue(editable)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Session> {
        override fun createFromParcel(parcel: Parcel): Session {
            return Session(parcel)
        }

        override fun newArray(size: Int): Array<Session?> {

            return arrayOfNulls(size)
        }
    }
}