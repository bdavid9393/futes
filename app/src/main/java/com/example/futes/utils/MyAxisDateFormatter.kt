package com.example.futes.utils

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class MyAxisDateFormatter : ValueFormatter() {
    val format1 = SimpleDateFormat("MM.dd")
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        var cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, value.toInt())
        return format1.format(cal.time)
    }

}
