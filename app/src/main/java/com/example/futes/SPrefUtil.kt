package com.example.futes

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import com.example.futes.data.AppDatabase
import kotlin.random.Random

class SPrefUtil {

    companion object {

        private var CUSTOM_PHONE_ID = "CUSTOM_PHONE_ID"
        private var IS_DATA_COLLECTOR = "IS_DATA_COLLECTOR"

        private var INSTANCE: SharedPreferences? = null

        fun getInstance(context: Context): SharedPreferences {
            if (INSTANCE == null) {
                INSTANCE =
                    PreferenceManager.getDefaultSharedPreferences(context)
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        fun getPhoneId(context: Context): Int {
            var num =  getInstance(context).getInt(CUSTOM_PHONE_ID, 0)
            if(num == 0){
                getInstance(context).edit().putInt(CUSTOM_PHONE_ID, getRandomInt()).apply()
                return getPhoneId(context)
            }
//            return num
            return  2223
        }

        fun isDataCollector(): Boolean {
            return INSTANCE!!.getBoolean(IS_DATA_COLLECTOR, false)
        }

        fun setDataCollector(bool: Boolean) {
            INSTANCE!!.edit().putBoolean(IS_DATA_COLLECTOR, bool).apply()
        }

        fun getRandomInt(): Int {
            return Random.nextInt(1000, 9999)
        }
    }
}