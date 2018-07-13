package jp.drjoy.app.domain.model.shift

import android.os.Parcel
import android.os.Parcelable
import com.example.long_pc.myapplication.model.ShiftItem
import java.util.*

class Shift() : Parcelable {
    var departmentId: String? = null
    var officeUserId: String? = null
    var userName: String? = null
    var teamDisplayOrder: String? = null
    var memberDisplayOrder: String? = null
    var isGuest: Boolean = false
    var guestDepartmentName: String? = null
    var memberId: String? = null
    var memberName: String? = null
    var teamColor: Int? = null
    var teamId: String? = null
    var teamShortName: String? = null
    var shiftList: ArrayList<ShiftItem>? = null

    constructor(parcel: Parcel) : this() {
        departmentId = parcel.readString()
        officeUserId = parcel.readString()
        userName = parcel.readString()
        teamDisplayOrder = parcel.readString()
        memberDisplayOrder = parcel.readString()
        isGuest = parcel.readValue(Boolean::class.java.classLoader) as Boolean
        memberId = parcel.readString()
        memberName = parcel.readString()
        officeUserId = parcel.readString()
        teamColor = parcel.readValue(Int::class.java.classLoader) as Int?
        teamId = parcel.readString()
        teamShortName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(departmentId)
        parcel.writeString(officeUserId)
        parcel.writeString(userName)
        parcel.writeString(teamDisplayOrder)
        parcel.writeString(memberDisplayOrder)
        parcel.writeValue(isGuest)
        parcel.writeString(guestDepartmentName)
        parcel.writeString(memberId)
        parcel.writeString(memberName)
        parcel.writeString(officeUserId)
        parcel.writeValue(teamColor)
        parcel.writeString(teamId)
        parcel.writeString(teamShortName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Shift> {
        override fun createFromParcel(parcel: Parcel): Shift {
            return Shift(parcel)
        }

        override fun newArray(size: Int): Array<Shift?> {
            return arrayOfNulls(size)
        }
    }
}