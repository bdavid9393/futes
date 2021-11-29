package com.example.futes

import android.R
import androidx.appcompat.app.AppCompatActivity

import android.os.Build.VERSION_CODES

import android.os.Build.VERSION

import android.util.Log

import android.os.*
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*
import com.example.futes.data.HeatLogRD
import com.example.futes.databinding.ActivityChartBinding
import com.example.futes.utils.BarFormatter
import com.example.futes.utils.MyAxisDateFormatter
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList
import java.util.Locale


class ChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding

    val database =
        Firebase.database.reference

    var dataList = arrayListOf<HeatLogRD?>()

    @RequiresApi(VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        importData()

        var phoneId = SPrefUtil.getPhoneId(this).toString()
        database.child(phoneId).get().addOnSuccessListener {

            var list = it.children.map { item ->
                item.getValue<HeatLogRD>()
            }.toList()
            dataList.addAll(list)
            Log.wtf("firebase", list.toString())
            processData()
        }.addOnFailureListener {
            Log.wtf("firebase", "Error getting data", it)
        }


        binding.chart.xAxis.mAxisMinimum = 0f
        binding.chart.xAxis.mAxisMaximum = 24f
        binding.chart.xAxis.valueFormatter = MyAxisDateFormatter()
        binding.chart.axisLeft.valueFormatter = BarFormatter()
        binding.chart.axisRight.valueFormatter = BarFormatter()


    }


    private fun processData() {
        var TAG = "CalcData"

        val minTimestamp = dataList.minByOrNull {
            it!!.timestamp!!
        }?.timestamp

        var firstDay = Calendar.getInstance()
        firstDay.time = Date(minTimestamp!! * 1000)

        var today = Calendar.getInstance()
        today.time = Date()

        Log.wtf(TAG, firstDay.toString())
        Log.wtf(TAG, today.toString())

        var days = arrayListOf<Calendar>(firstDay)

        while (days.get(days.size - 1).get(Calendar.DAY_OF_YEAR) !=
            today.get(Calendar.DAY_OF_YEAR)
        ) {
            val lastDay = Calendar.getInstance()
            lastDay.time = days.get(days.size - 1).time
            lastDay.add(Calendar.DATE, 1)
            days.add(lastDay)
            Log.wtf(TAG, lastDay.toString())
        }

        var dataMap = TreeMap<Int, ArrayList<HeatLogRD>>()

        days.forEach { day ->
            dataList.forEach { log ->
                var logDate = Calendar.getInstance()
                logDate.time = Date(log!!.timestamp!! * 1000)
                if (day.get(Calendar.DAY_OF_YEAR) == logDate.get(Calendar.DAY_OF_YEAR)) {
                    var dayOfYear = day.get(Calendar.DAY_OF_YEAR)
                    if (dataMap.containsKey(dayOfYear)) {
                        var list: ArrayList<HeatLogRD> =
                            dataMap.get(dayOfYear) as ArrayList<HeatLogRD>
                        list.add(log)
                    } else {
                        dataMap.put(dayOfYear, arrayListOf(log))
                    }
                }

            }

        }
        Log.wtf(TAG, dataMap.toString())
        val chart = binding.chart
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            var barDataList = arrayListOf<BarDataSet>()
            dataMap.forEach { dayOfYear, externalData ->
                var dataList = arrayListOf<BarEntry>()
                externalData.sortBy { it.timestamp }

                var floatArray = arrayListOf<Float>()

                for (i in 0..externalData.size - 1) {
                    var item = externalData.get(i)
                    if (i == 0) {
                        floatArray.add(item.getHourOfDay)
                    } else {
                        var prevItem = externalData.get(i - 1)
                        if (prevItem.offEvent == item.offEvent) {
                            continue
                        }
                        floatArray.add(item.getHourOfDay - prevItem.getHourOfDay)
                    }
                    if (externalData.size - 1 == i) {
                        floatArray.add(24f - item.getHourOfDay)
                    }
                }


                dataList.add(
                    BarEntry(
                        dayOfYear.toFloat(),
                        floatArray.toFloatArray()
                    )
                )
                var barDataSet = BarDataSet(dataList, "Az év:" + dayOfYear.toString() + ". napja")

                if (externalData.get(0).offEvent == false) {
                    barDataSet.setColors(
                        getColor(R.color.transparent),
                        getColor(R.color.holo_blue_light)
                    )
                } else {
                    barDataSet.setColors(
                        getColor(R.color.holo_blue_light),
                        getColor(R.color.transparent),
                    )
                }

                barDataSet.valueFormatter = BarFormatter()
                barDataList.add(barDataSet)
            }


            var barData = BarData()
            barDataList.forEach({
                barData.addDataSet(it)
            })

            chart.data = barData
            chart.invalidate()
        }


    }


    private fun importData() {
        var data = """
          2021.11.19 20:01:27;Bekapcsolt
2021.11.19 20:01:29;Kikapcsolt
2021.11.20 06:04:19;Bekapcsolt
2021.11.20 07:02:38;Kikapcsolt
2021.11.20 07:47:59;Bekapcsolt
2021.11.20 08:14:36;Kikapcsolt
2021.11.20 10:16:12;Bekapcsolt
2021.11.20 10:42:41;Kikapcsolt
2021.11.20 14:40:24;Bekapcsolt
2021.11.20 14:54:06;Kikapcsolt
2021.11.20 17:11:43;Bekapcsolt
2021.11.20 17:35:44;Kikapcsolt
2021.11.20 20:02:57;Bekapcsolt
2021.11.20 20:23:00;Kikapcsolt
2021.11.21 03:56:06;Bekapcsolt
2021.11.21 04:21:42;Kikapcsolt
2021.11.21 07:01:46;Bekapcsolt
2021.11.21 07:59:05;Kikapcsolt
2021.11.21 09:16:58;Bekapcsolt
2021.11.21 09:41:34;Kikapcsolt
2021.11.21 11:25:23;Bekapcsolt
2021.11.21 11:36:23;Kikapcsolt
2021.11.21 16:35:50;Bekapcsolt
2021.11.21 17:13:26;Kikapcsolt
2021.11.21 18:58:30;Bekapcsolt
2021.11.21 19:24:40;Kikapcsolt
2021.11.22 03:47:19;Bekapcsolt
2021.11.22 04:13:17;Kikapcsolt
2021.11.22 06:36:25;Bekapcsolt
2021.11.22 07:25:01;Kikapcsolt
2021.11.22 07:55:30;Bekapcsolt
2021.11.22 08:25:03;Kikapcsolt
2021.11.22 09:37:43;Bekapcsolt
2021.11.22 10:06:15;Kikapcsolt
2021.11.22 11:47:43;Bekapcsolt
2021.11.22 12:15:10;Kikapcsolt
2021.11.22 13:50:38;Bekapcsolt
2021.11.22 14:18:27;Kikapcsolt
2021.11.22 15:57:14;Bekapcsolt
2021.11.22 16:23:46;Kikapcsolt
2021.11.22 17:04:18;Bekapcsolt
2021.11.22 17:27:22;Kikapcsolt
2021.11.22 18:45:13;Bekapcsolt
2021.11.22 19:13:22;Kikapcsolt
2021.11.22 20:39:12;Bekapcsolt
2021.11.22 20:39:31;Kikapcsolt
2021.11.23 00:43:43;Bekapcsolt
2021.11.23 01:08:57;Kikapcsolt
2021.11.23 03:20:42;Bekapcsolt
2021.11.23 03:45:44;Kikapcsolt
2021.11.23 05:38:40;Bekapcsolt
2021.11.23 06:04:35;Kikapcsolt
2021.11.23 06:48:50;Bekapcsolt
2021.11.23 07:25:01;Kikapcsolt
2021.11.23 08:47:36;Bekapcsolt
2021.11.23 09:11:19;Kikapcsolt
2021.11.23 12:59:22;Bekapcsolt
2021.11.23 13:22:53;Kikapcsolt
2021.11.23 15:09:29;Bekapcsolt
2021.11.23 15:37:36;Kikapcsolt
2021.11.23 16:59:11;Bekapcsolt
2021.11.23 17:23:26;Kikapcsolt
2021.11.23 17:53:17;Bekapcsolt
2021.11.23 18:18:25;Kikapcsolt
2021.11.23 22:46:52;Bekapcsolt
2021.11.23 23:13:09;Kikapcsolt
2021.11.24 01:15:00;Bekapcsolt
2021.11.24 01:40:47;Kikapcsolt
2021.11.24 03:23:12;Bekapcsolt
2021.11.24 03:49:38;Kikapcsolt
2021.11.24 05:23:33;Bekapcsolt
2021.11.24 05:49:57;Kikapcsolt
2021.11.24 06:49:55;Bekapcsolt
2021.11.24 07:19:48;Kikapcsolt
2021.11.24 08:00:08;Bekapcsolt
2021.11.24 08:29:54;Kikapcsolt
2021.11.24 12:07:09;Bekapcsolt
2021.11.24 12:29:48;Kikapcsolt
2021.11.24 14:48:54;Bekapcsolt
2021.11.24 15:13:20;Kikapcsolt
2021.11.24 16:54:27;Bekapcsolt
2021.11.24 17:34:38;Kikapcsolt
2021.11.24 20:11:01;Bekapcsolt
2021.11.24 20:30:18;Kikapcsolt
2021.11.25 00:41:12;Bekapcsolt
2021.11.25 01:05:33;Kikapcsolt
2021.11.25 03:02:48;Bekapcsolt
2021.11.25 03:29:16;Kikapcsolt
2021.11.25 05:11:57;Bekapcsolt
2021.11.25 05:37:35;Kikapcsolt
2021.11.25 06:00:52;Bekapcsolt
2021.11.25 06:35:06;Kikapcsolt
2021.11.25 07:07:40;Bekapcsolt
2021.11.25 07:40:02;Kikapcsolt
2021.11.25 08:29:42;Bekapcsolt
2021.11.25 09:01:36;Kikapcsolt
2021.11.25 10:05:06;Bekapcsolt
2021.11.25 10:34:36;Kikapcsolt
2021.11.25 11:47:24;Bekapcsolt
2021.11.25 12:15:00;Kikapcsolt
2021.11.25 13:33:04;Bekapcsolt
2021.11.25 14:07:12;Kikapcsolt
2021.11.25 15:00:45;Bekapcsolt
2021.11.25 15:25:58;Kikapcsolt
2021.11.25 17:03:19;Bekapcsolt
2021.11.25 17:37:59;Kikapcsolt
2021.11.25 19:29:18;Bekapcsolt
2021.11.25 19:58:00;Kikapcsolt
2021.11.26 00:19:11;Bekapcsolt
2021.11.26 00:46:42;Kikapcsolt
2021.11.26 02:46:33;Bekapcsolt
2021.11.26 03:12:26;Kikapcsolt
2021.11.26 05:07:46;Bekapcsolt
2021.11.26 05:33:57;Kikapcsolt
2021.11.26 06:00:22;Bekapcsolt
2021.11.26 06:34:08;Kikapcsolt
2021.11.26 07:10:28;Bekapcsolt
2021.11.26 07:49:43;Kikapcsolt
2021.11.26 08:47:10;Bekapcsolt
2021.11.26 09:18:09;Kikapcsolt
2021.11.26 10:23:18;Bekapcsolt
2021.11.26 10:54:27;Kikapcsolt
2021.11.26 12:03:57;Bekapcsolt
2021.11.26 12:31:38;Kikapcsolt
2021.11.26 14:05:32;Bekapcsolt
2021.11.26 14:34:35;Kikapcsolt
2021.11.26 15:58:13;Bekapcsolt
2021.11.26 16:23:58;Kikapcsolt
2021.11.26 18:12:32;Bekapcsolt
2021.11.26 18:28:59;Kikapcsolt
2021.11.26 20:22:53;Bekapcsolt
2021.11.26 20:30:24;Kikapcsolt
2021.11.27 00:04:37;Bekapcsolt
2021.11.27 00:30:06;Kikapcsolt
2021.11.27 02:36:05;Bekapcsolt
2021.11.27 03:01:42;Kikapcsolt
2021.11.27 04:47:49;Bekapcsolt
2021.11.27 05:13:26;Kikapcsolt
2021.11.27 06:00:25;Bekapcsolt
2021.11.27 06:39:39;Kikapcsolt
2021.11.27 07:14:20;Bekapcsolt
2021.11.27 07:48:30;Kikapcsolt
2021.11.27 08:19:27;Bekapcsolt
2021.11.27 08:47:09;Kikapcsolt
2021.11.27 09:46:47;Bekapcsolt
2021.11.27 10:10:55;Kikapcsolt
2021.11.27 12:10:26;Bekapcsolt
2021.11.27 12:44:41;Kikapcsolt
2021.11.27 13:56:53;Bekapcsolt
2021.11.27 14:31:11;Kikapcsolt
2021.11.27 15:37:31;Bekapcsolt
2021.11.27 16:05:26;Kikapcsolt
2021.11.27 17:28:16;Bekapcsolt
2021.11.27 17:59:22;Kikapcsolt
2021.11.27 19:46:04;Bekapcsolt
2021.11.27 20:12:35;Kikapcsolt
2021.11.28 00:23:21;Bekapcsolt
2021.11.28 00:47:36;Kikapcsolt
2021.11.28 03:04:17;Bekapcsolt
2021.11.28 03:30:11;Kikapcsolt
2021.11.28 05:30:03;Bekapcsolt
2021.11.28 05:55:30;Kikapcsolt
2021.11.28 06:22:34;Bekapcsolt
2021.11.28 06:54:13;Kikapcsolt
2021.11.28 07:31:15;Bekapcsolt
2021.11.28 08:14:49;Kikapcsolt
2021.11.28 09:16:50;Bekapcsolt
2021.11.28 09:47:06;Kikapcsolt
2021.11.28 11:01:20;Bekapcsolt
2021.11.28 11:01:34;Kikapcsolt
2021.11.28 11:01:38;Bekapcsolt
2021.11.28 11:28:05;Kikapcsolt
2021.11.28 13:06:09;Bekapcsolt
2021.11.28 13:33:53;Kikapcsolt
2021.11.28 16:01:18;Bekapcsolt
2021.11.28 16:28:42;Kikapcsolt
2021.11.28 17:59:39;Bekapcsolt
2021.11.28 18:17:16;Kikapcsolt
2021.11.28 20:00:36;Bekapcsolt
2021.11.28 20:26:47;Kikapcsolt
2021.11.29 01:16:39;Bekapcsolt
2021.11.29 01:41:29;Kikapcsolt
2021.11.29 03:47:54;Bekapcsolt
2021.11.29 04:12:51;Kikapcsolt
2021.11.29 06:00:24;Bekapcsolt
2021.11.29 06:48:58;Kikapcsolt
        """.trimIndent()
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        data.split('\n').forEach({
            it.split(';').let {
                var dto = HeatLogRD(
                    formatter.parse(it.get(0)).time / 1000,
                    it.get(1).trim().equals("Kikapcsolt")
                )
                Log.wtf("input", dto.toString())
                var phoneId = "2223"
                var newPostKey = database.child(phoneId).push().key
                database.child(phoneId).child(newPostKey!!).setValue(dto)
            }
        })
    }

}