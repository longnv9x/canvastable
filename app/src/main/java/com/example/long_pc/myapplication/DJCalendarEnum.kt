package com.example.long_pc.myapplication

import com.google.gson.annotations.SerializedName

class DJCalendarEnum {
    enum class Type constructor(val id: Int?) {
        @SerializedName("0")
        MY(0),

        @SerializedName("1")
        STAFF(1),

        @SerializedName("2")
        INSIDE_GROUP(2),

        @SerializedName("3")
        OUTSIDE_GROUP(3),

        @SerializedName("4")
        MEETING_DECIDED(4),

        @SerializedName("5")
        MEETING_ACCEPTING(5),

        @SerializedName("6")
        MEETING_MEDIATOR(6),

        @SerializedName("7")
        BRIEFING(7),

        @SerializedName("8")
        VISIT(8),

        @SerializedName("9")
        SOCIETY(9),

        @SerializedName("10")
        HOLIDAY(10),

        @SerializedName("11")
        EXTERNAL(11),

        @SerializedName("12")
        RQ_MEETING_MEDIATOR(12);

        companion object {
            fun valueOf(value: Int?): Type? {
                return values()
                        .firstOrNull { it.id == value }
            }
        }
    }

    enum class Authority constructor(val id: Int?) {

        @SerializedName("0")
        DENY(0),

        @SerializedName("1")
        SCHEDULE_ONLY(1),

        @SerializedName("2")
        READ_ONLY(2),

        @SerializedName("3")
        EDITABLE(3)
    }

    enum class MEMeetingFrameType(name: String) {

        @SerializedName("0")
        SPOT("0"),

        @SerializedName("1")
        SLOTS("1"),

        @SerializedName("UNRECOGNIZED")
        UNRECOGNIZED("UNRECOGNIZED")
    }
}