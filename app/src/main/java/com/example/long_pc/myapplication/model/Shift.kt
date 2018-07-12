package jp.drjoy.app.domain.model.shift

import android.os.Parcel
import android.os.Parcelable
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
    var teamColor: String? = null
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
        teamColor = parcel.readString()
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
        parcel.writeString(teamColor)
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

    class ShiftItem() : Parcelable {
        var date: Date? = null
        var morning: Session? = null
        var afternoon: Session? = null
        var night: Session? = null

        constructor(parcel: Parcel) : this() {
            morning = parcel.readParcelable(Session::class.java.classLoader)
            afternoon = parcel.readParcelable(Session::class.java.classLoader)
            night = parcel.readParcelable(Session::class.java.classLoader)
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(morning, flags)
            parcel.writeParcelable(afternoon, flags)
            parcel.writeParcelable(night, flags)
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

    class Session() : Parcelable {
        var id: String? = null
        var departmentId: String? = null
        var category: String? = null
        var shortName: String? = null
        var name: String? = null
        var color: String? = null
        var editable: Boolean? = null

        constructor(parcel: Parcel) : this() {
            id = parcel.readString()
            departmentId = parcel.readString()
            category = parcel.readString()
            shortName = parcel.readString()
            name = parcel.readString()
            color = parcel.readString()
            editable = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(departmentId)
            parcel.writeString(category)
            parcel.writeString(shortName)
            parcel.writeString(name)
            parcel.writeString(color)
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
}