package com.example.long_pc.myapplication

import java.util.*

object ColorUtils {

    val DEFAULT_COLOR = "#EF8E8E"

    val listColorToUserSelect: ArrayList<String> = arrayListOf(
            "#D576E5", "#FF76BB", "#DB4678", "#EF4B4B", "#FF7537", "#FFA412",
            "#14AF65", "#7BD148", "#42CE88", "#2BC1A4", "#4CC5E5", "#3C96EF",
            "#938BD1", "#EF8E8E", "#DDB50E", "#AFA168", "#AF9380", "#B77777",
            "#BA8590", "#C997BA", "#8793AA", "#809E8C", "#A0A0A0", "#BCBCBC"
    )

    val listColorRandom: ArrayList<String> = arrayListOf(
            "#ED88CE", "#DD3AA3", "#B5304F", "#D81919", "#F46D08", "#FFA412",
            "#EDBF00", "#BFE27D", "#8FC31F", "#22AC38", "#006934", "#00A29A",
            "#5BBBDD", "#4A94D8", "#036EB8", "#0037FF", "#834BBF", "#A78FCC",
            "#A40B5D", "#915050", "#A48B78", "#956134", "#6D5A49", "#898989",
            "#867491", "#C997BA", "#AFA168", "#708E7E", "#69738C", "#323232"
    )

    /**
     * Use for AP4015
     */
    fun getRandomColor(): String {
        val random = Random()
        val indexRandom = random.nextInt(listColorRandom.size)
        return listColorRandom[indexRandom]
    }

    fun convertFromIntToHex(color: Int?): String {
        val colorString = color?.toString(16)

        return when {
            colorString == null -> {
                DEFAULT_COLOR
            }
            colorString.length < 6 -> {
                val numberZeroNeedToAdd = 6 - colorString.length
                val stringBuilder = StringBuilder("#")
                for (i in 1..numberZeroNeedToAdd) {
                    stringBuilder.append("0")
                }
                stringBuilder.append(colorString).toString()
            }
            else -> {
                StringBuilder("#").append(colorString).toString()
            }
        }
    }

    fun convertFromHexToInt(color: String?): Int? {
        return color?.substring(1)?.toInt(16)
    }
}