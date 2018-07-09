package com.example.long_pc.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList


open class User() : Parcelable {

    companion object CREATOR : Parcelable.Creator<User> {

        @JvmStatic
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        @JvmStatic
        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

        const val TEMPORARY_REGISTER = 1

        const val VALID = 2

        const val LOCKED = 4

        const val INVALID = 8

        @JvmStatic
        fun equalSkeleton(first: User, second: User): Boolean {
            if (first.officeUserId != second.officeUserId) {
                return false
            }

            return true
        }

        @JvmStatic
        fun <T : User> comparatorUser(user1: T, user2: T): Int {
            //compare by invalid accounts
            if (user1.accountInvalid && !user2.accountInvalid) return 1
            if (!user1.accountInvalid && user2.accountInvalid) return -1

            //compare by nameKana
            if ((user1.nameKana != null) && (user2.nameKana == null)) return 1
            if ((user1.nameKana == null) && (user2.nameKana != null)) return -1

            if (user1.nameKana!! > user2.nameKana!!) return 1
            if (user1.nameKana!! < user2.nameKana!!) return -1

            return 0
        }

        @JvmStatic
        fun <T : User> comparatorUserInRoom(user1: T, user2: T): Int {
            //compare by invalid accounts
            if (user1.accountInvalid && !user2.accountInvalid) return 1
            if (!user1.accountInvalid && user2.accountInvalid) return -1
            return 0
        }
    }

    /**
     * ユーザーID
     */
    var id: String? = null


    var officeUserId: String? = null

    /**
     * ユーザー名
     */
    var name: String? = null

    /**
     * ユーザー名(かな)
     */
    var nameKana: String? = null

    /**
     * 病院・会社名ID
     */
    var officeId: String? = null

    /**
     * 病院・会社名
     */
    var office: String? = null

    /**
     * 所属ID
     */
    var departmentId: String? = null

    /**
     * 所属
     */
    var department: String? = null

    /**
     * 所属(全階層表示)
     */
    var departmentFull: String? = null

    /**
     * アカウントロックフラグ
     */
    val accountLock: Boolean
        get() {
            return accountStatus?.and(LOCKED) != 0
        }

    val accountValid: Boolean
        get() {
            return accountStatus?.and(VALID) != 0
        }

    val accountInvalid: Boolean
        get() {
            return accountStatus?.and(INVALID) ?: 0 != 0
        }

    val accountTemp: Boolean
        get() {
            return accountStatus?.and(TEMPORARY_REGISTER) != 0
        }
    /**
     * 管理者権限
     * 1:全体管理者
     * 2:所属管理者
     * 3:権限なし
     */
    var mngAuthority: Int? = null

    /**
     * その他権限
     * bitフラグ
     * FP_1  1<<0  面会／説明会機能を使うことができる
     * FP_2  1<<1  病院の面会ルールを設定することができる
     * FP_3  1<<2  面会状況を閲覧・ダウンロードできる
     * FP_4  1<<3  説明会状況を閲覧・ダウンロードできる
     * FP_5  1<<4  取引先一覧をダウンロードすることができる
     * FP_6  1<<5  取引先への一斉配信ができる
     * FP_7  1<<6  仲介者の担当医師を設定し、仲介設定した医師の面会を管理できる
     */
    var funcAuthority: Int? = null
        get() {
            functionAuthorityDetail?.forEach {
                //#9596
                if (it == 1)
                    return 1
            }
            return 0
        }

    var functionAuthorityDetail: ArrayList<Int>? = null

    /**
     * 性別
     */
    var gender: Int? = null

    /**
     * 職業
     */
    var jobType: String? = null

    var jobTypeName: String? = null

    /**
     * 連絡先
     */
    var phoneNo: String? = null

    /**
     * 携帯連絡先
     */
    var mobilePhoneNo: String? = null

    /**
     * アドレス
     */
    var mailAddress: String? = null

    /**
     * PHS
     */
    var phsNo: String? = null

    /**
     * 経験年数
     */
    var experiences: Int? = null

    /**
     * Value date graduation with user is Dr (Staff - AP7008)
     */
    var graduationDate: Date? = null

    /**
     * 診療科一覧
     */
    var specializedDepartment: ArrayList<SpecializedDepartment>? = null

    /**
     * 略歴
     */
    var biography: String? = null

    /**
     * 学会・資格
     */
    var qualification: String? = null

    /**
     * 役職・役割
     */
    var position: String? = null

    /**
     * 趣味
     */
    var hobby: String? = null

    /**
     * 出身地
     */
    var placeBornIn: String? = null

    var favoriteContents: ArrayList<String> = ArrayList()

    var memberId: String? = null

    var accountStatus: Int? = 0

    var firstName: String? = null

    var lastName: String? = null

    var firstNameKana: String? = null

    var lastNameKana: String? = null

    var officeName: String? = null

    var imageUrl: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        officeUserId = parcel.readString()
        name = parcel.readString()
        nameKana = parcel.readString()
        officeId = parcel.readString()
        office = parcel.readString()
        departmentId = parcel.readString()
        department = parcel.readString()
        departmentFull = parcel.readString()
        mngAuthority = parcel.readValue(Int::class.java.classLoader) as? Int
        funcAuthority = parcel.readValue(Int::class.java.classLoader) as? Int
        gender = parcel.readValue(Int::class.java.classLoader) as? Int
        jobType = parcel.readString()
        jobTypeName = parcel.readString()
        phoneNo = parcel.readString()
        mobilePhoneNo = parcel.readString()
        mailAddress = parcel.readString()
        phsNo = parcel.readString()
        experiences = parcel.readValue(Int::class.java.classLoader) as? Int
        graduationDate = parcel.readSerializable() as Date?
        parcel.readList(specializedDepartment, SpecializedDepartment::class.java.classLoader)
        biography = parcel.readString()
        qualification = parcel.readString()
        position = parcel.readString()
        hobby = parcel.readString()
        placeBornIn = parcel.readString()
        parcel.readList(favoriteContents, String::class.java.classLoader)
        memberId = parcel.readString()
        accountStatus = parcel.readValue(Int::class.java.classLoader) as? Int
        firstName = parcel.readString()
        lastName = parcel.readString()
        firstNameKana = parcel.readString()
        lastNameKana = parcel.readString()
        officeName = parcel.readString()
        imageUrl = parcel.readString()
    }

    constructor(officeUserId: String) : this() {
        this.officeUserId = officeUserId
    }


    fun <T : User> copy(user: T) {
        this.id = user.id
        this.officeUserId = user.officeUserId
        this.name = user.name
        this.nameKana = user.nameKana
        this.officeId = user.officeId
        this.office = user.office
        this.departmentId = user.departmentId
        this.department = user.department
        this.departmentFull = user.departmentFull
        this.mngAuthority = user.mngAuthority
        this.funcAuthority = user.funcAuthority

        this.gender = user.gender
        this.jobType = user.jobType
        this.jobTypeName = user.jobTypeName
        this.phoneNo = user.phoneNo
        this.mobilePhoneNo = user.mobilePhoneNo
        this.mailAddress = user.mailAddress
        this.phsNo = user.phsNo
        this.experiences = user.experiences
        this.graduationDate = user.graduationDate

        this.specializedDepartment = ArrayList()
        user.specializedDepartment?.forEach { modelDepartment ->
            val department = SpecializedDepartment()
            department.field = modelDepartment.field
            department.fieldName = modelDepartment.fieldName
            department.type = modelDepartment.type
            department.typeName = modelDepartment.typeName

            this.specializedDepartment!!.add(department)
        }

        this.biography = user.biography
        this.qualification = user.qualification
        this.position = user.position
        this.hobby = user.hobby
        this.placeBornIn = user.placeBornIn
        this.favoriteContents.addAll(user.favoriteContents)
        this.memberId = user.memberId
        this.accountStatus = user.accountStatus
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.firstNameKana = user.firstNameKana
        this.lastNameKana = user.lastNameKana
        this.officeName = user.officeName
        this.imageUrl = user.imageUrl
    }

    override fun equals(other: Any?): Boolean {
        if (other is User) {

            if (this.id != other.id) {
                return false
            }
            if (this.officeUserId != other.officeUserId) {
                return false
            }
            if (this.name != other.name) {
                return false
            }
            if (this.nameKana != other.nameKana) {
                return false
            }
            if (this.officeId != other.officeId) {
                return false
            }
            if (this.office != other.office) {
                return false
            }
            if (this.departmentId != other.departmentId) {
                return false
            }
            if (this.department != other.department) {
                return false
            }
            if (this.departmentFull != other.departmentFull) {
                return false
            }
            if (this.mngAuthority != other.mngAuthority) {
                return false
            }
            if (this.funcAuthority != other.funcAuthority) {
                return false
            }
            if (this.gender != other.gender) {
                return false
            }
            if (this.jobType != other.jobType) {
                return false
            }
            if (this.jobTypeName != other.jobTypeName) {
                return false
            }
            if (this.phoneNo != other.phoneNo) {
                return false
            }
            if (this.mobilePhoneNo != other.mobilePhoneNo) {
                return false
            }
            if (this.mailAddress != other.mailAddress) {
                return false
            }
            if (this.phsNo != other.phsNo) {
                return false
            }
            if (this.experiences != other.experiences) {
                return false
            }
            if (graduationDate != other.graduationDate) {
                return false
            }
            if (this.specializedDepartment != other.specializedDepartment) {
                return false
            }
            if (this.biography != other.biography) {
                return false
            }
            if (this.qualification != other.qualification) {
                return false
            }
            if (this.position != other.position) {
                return false
            }
            if (this.hobby != other.hobby) {
                return false
            }
            if (this.placeBornIn != other.placeBornIn) {
                return false
            }
            if (this.favoriteContents != other.favoriteContents) {
                return false
            }
            if (this.memberId != other.memberId) {
                return false
            }
            if (this.accountStatus != other.accountStatus) {
                return false
            }
            if (this.firstName != other.firstName) {
                return false
            }
            if (this.lastName != other.lastName) {
                return false
            }
            if (this.firstNameKana != other.firstNameKana) {
                return false
            }
            if (this.lastNameKana != other.lastNameKana) {
                return false
            }
            if (this.officeName != other.officeName) {
                return false
            }
            if (this.imageUrl != other.imageUrl) {
                return false
            }

            return true
        }
        return super.equals(other)
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(officeUserId)
        parcel.writeString(name)
        parcel.writeString(nameKana)
        parcel.writeString(officeId)
        parcel.writeString(office)
        parcel.writeString(departmentId)
        parcel.writeString(department)
        parcel.writeString(departmentFull)
        parcel.writeValue(mngAuthority)
        parcel.writeValue(funcAuthority)
        parcel.writeValue(gender)
        parcel.writeString(jobType)
        parcel.writeString(jobTypeName)
        parcel.writeString(phoneNo)
        parcel.writeString(mobilePhoneNo)
        parcel.writeString(mailAddress)
        parcel.writeString(phsNo)
        parcel.writeValue(experiences)
        parcel.writeSerializable(graduationDate)
        parcel.writeList(specializedDepartment)
        parcel.writeString(biography)
        parcel.writeString(qualification)
        parcel.writeString(position)
        parcel.writeString(hobby)
        parcel.writeString(placeBornIn)
        parcel.writeList(favoriteContents)
        parcel.writeString(memberId)
        parcel.writeValue(accountStatus)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(firstNameKana)
        parcel.writeString(lastNameKana)
        parcel.writeString(officeName)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun equalsFirebase(user: User): Boolean {
        if (this.officeUserId != user.officeUserId) {
            return false
        }
        if (this.firstName != user.firstName) {
            return false
        }
        if (this.lastName != user.lastName) {
            return false
        }
        if (this.firstNameKana != user.firstNameKana) {
            return false
        }
        if (this.lastNameKana != user.lastNameKana) {
            return false
        }
        if (this.officeId != user.officeId) {
            return false
        }
        if (this.office != user.office) {
            return false
        }
        if (this.departmentId != user.departmentId) {
            return false
        }
        if (this.department != user.department) {
            return false
        }
        if (this.accountStatus != user.accountStatus) {
            return false
        }
        if (this.imageUrl != user.imageUrl) {
            return false
        }

        return true
    }

    class SpecializedDepartment() : Parcelable {

        companion object CREATOR : Parcelable.Creator<SpecializedDepartment> {
            override fun createFromParcel(parcel: Parcel): SpecializedDepartment {
                return SpecializedDepartment(parcel)
            }

            override fun newArray(size: Int): Array<SpecializedDepartment?> {
                return arrayOfNulls(size)
            }
        }

        var field: String? = null
        var fieldName: String? = null
        var type: String? = null
        var typeName: String? = null

        constructor(parcel: Parcel) : this() {
            field = parcel.readString()
            fieldName = parcel.readString()
            type = parcel.readString()
            typeName = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(field)
            parcel.writeString(fieldName)
            parcel.writeString(type)
            parcel.writeString(typeName)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    class Department {
        val id: String? = null
        val name: String? = null
        val displayName: String? = null
        val children: List<Department>? = null
    }
}