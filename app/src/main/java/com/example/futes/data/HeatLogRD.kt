package com.example.futes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

data class HeatLogRD(
    val timestamp: Long? = 0,
    val offEvent: Boolean? = false
) {

    val getMiutesOfDay: Float
        get() {
            var cal = Calendar.getInstance()
            cal.time = Date(timestamp!! * 1000)
            return cal.get(Calendar.HOUR_OF_DAY).toFloat() + cal.get(Calendar.MINUTE).toFloat() / 60
        }
}


