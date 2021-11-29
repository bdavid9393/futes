package com.example.futes.utils

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*

class BarFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        var label = ""
        var wholeNum = value.toInt()

        if (wholeNum != 0) {
            label = wholeNum.toString() + " ó "
        }

        label = label + ((value - wholeNum) * 60f).toInt().toString() + " p"
        return label
    }


    override fun getBarStackedLabel(value: Float, stackedEntry: BarEntry?): String {
        var label = ""
        var wholeNum = value.toInt()

        if (wholeNum != 0) {
            label = wholeNum.toString() + " ó "
        }

        label = label + ((value - wholeNum) * 60f).toInt().toString() + " p"
        return label
    }


}
