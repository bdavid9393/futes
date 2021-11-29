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

        importData()

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
        """.trimIndent()
        val formatter = SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Locale.getDefault())
        data.split('\n').forEach({
            it.split(';').let {
                var dto = HeatLogRD(
                    formatter.parse(it.get(0)).time / 1000,
                    it.get(1).trim().equals("Kikapcsolt")
                )
                Log.wtf("input", dto.toString())
                var phoneId = "2222"
                var newPostKey = database.child(phoneId).child((dto.timestamp).toString())
                newPostKey.setValue(
                    dto
                )
            }
        })
    }

}