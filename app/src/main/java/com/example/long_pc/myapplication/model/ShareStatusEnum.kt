package com.example.long_pc.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * Created by Admin on 2/4/2018.
 */
enum class ShareStatusEnum(val level: Int) {
    @SerializedName("3")
    EDIT(3),

    @SerializedName("2")
    VIEW_ALL(2),

    @SerializedName("1")
    VIEW_ONLY_TIME(1);

    companion object {
        fun valueOf(level: Int?): ShareStatusEnum? {
            return values()
                    .firstOrNull { it.level == level }
        }
    }
}