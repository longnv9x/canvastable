package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import jp.drjoy.app.domain.model.shift.Shift

class Staff() : Parcelable {
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

    constructor(parcel: Parcel) : this() {
        departmentId = parcel.readString()
        officeUserId = parcel.readString()
        userName = parcel.readString()
        teamDisplayOrder = parcel.readString()
        memberDisplayOrder = parcel.readString()
        isGuest = parcel.readByte() != 0.toByte()
        guestDepartmentName = parcel.readString()
        memberId = parcel.readString()
        memberName = parcel.readString()
        teamColor = parcel.readValue(Int::class.java.classLoader) as? Int
        teamId = parcel.readString()
        teamShortName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(departmentId)
        parcel.writeString(officeUserId)
        parcel.writeString(userName)
        parcel.writeString(teamDisplayOrder)
        parcel.writeString(memberDisplayOrder)
        parcel.writeByte(if (isGuest) 1 else 0)
        parcel.writeString(guestDepartmentName)
        parcel.writeString(memberId)
        parcel.writeString(memberName)
        parcel.writeValue(teamColor)
        parcel.writeString(teamId)
        parcel.writeString(teamShortName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Staff> {
        override fun createFromParcel(parcel: Parcel): Staff {
            return Staff(parcel)
        }

        override fun newArray(size: Int): Array<Staff?> {
            return arrayOfNulls(size)
        }

        fun convertFormEntity(event: Shift): Staff {
            return Staff().apply {
                departmentId = event.departmentId
                officeUserId = event.officeUserId
                userName = event.userName
                teamDisplayOrder = event.teamDisplayOrder
                memberDisplayOrder = event.memberDisplayOrder
                isGuest = event.isGuest
                guestDepartmentName = event.guestDepartmentName
                memberId = event.memberId
                memberName = event.memberName
                teamColor = event.teamColor
                teamId = event.teamId
                teamShortName = event.teamShortName
            }
        }
    }
}