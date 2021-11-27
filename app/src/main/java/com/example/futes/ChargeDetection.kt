package com.example.futes

import android.content.BroadcastReceiver
import android.content.Context
import com.example.futes.CheckUtil
import android.content.Intent
import android.util.Log
import com.example.futes.data.AppDatabase
import com.example.futes.data.HeatLog
import com.example.futes.data.HeatLogRD
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

internal class ChargeDetection : BroadcastReceiver() {
    var checkUtil = CheckUtil()
    val database =
        Firebase.database.reference

    override fun onReceive(context: Context, intent: Intent) {
        Log.wtf("BATTERY", checkUtil.isPlugged(context).toString())
        //Now check if user is charging here. This will only run when device is either plugged in or unplugged.

        val date = Date()
        val state: String
        if (checkUtil.isPlugged(context)) {
            state = "Kikapcsolt"
        } else {
            state = "Bekapcsolt"
        }
        val heatLog = HeatLog(date.time / 1000, date.time, state)

        AppDatabase.getInstance(context).heatLogDao().insert(heatLog)

        var phoneId = SPrefUtil.getPhoneId(context).toString()
        var newPostKey = database.child(phoneId).child((date.time / 1000).toString())
        newPostKey.setValue(
            HeatLogRD(date.time / 1000, checkUtil.isPlugged(context))
        )
    }
}