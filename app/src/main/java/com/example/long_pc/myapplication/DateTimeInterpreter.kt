package com.example.long_pc.myapplication

import java.util.*

interface DateTimeInterpreter {
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}