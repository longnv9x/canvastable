package jp.drjoy.app.domain.model.shift

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Ominext on 6/15/2018.
 */
class Memo() : Parcelable {
    var privateNote: String? = null
    var publicNote: String? = null
    var departmentId: String? = null
    var officeUserId: String? = null
    var targetDate: Date? = null
    var isLoaded: Boolean = false

    constructor(parcel: Parcel) : this() {
        privateNote = parcel.readString()
        publicNote = parcel.readString()
        departmentId = parcel.readString()
        officeUserId = parcel.readString()
        isLoaded = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(privateNote)
        parcel.writeString(publicNote)
        parcel.writeString(departmentId)
        parcel.writeString(officeUserId)
        parcel.writeByte(if (isLoaded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Memo> {
        override fun createFromParcel(parcel: Parcel): Memo {
            return Memo(parcel)
        }

        override fun newArray(size: Int): Array<Memo?> {
            return arrayOfNulls(size)
        }
    }
}