package com.example.futes

import android.R
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

import android.os.Build.VERSION_CODES

import android.os.Build.VERSION

import android.content.Intent

import android.content.IntentFilter
import android.os.health.HealthStats
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.room.Room
import com.example.futes.data.AppDatabase
import com.example.futes.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import android.widget.Toast

import android.content.DialogInterface
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AlertDialog
import com.example.futes.data.HeatLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.FileProvider
import com.example.futes.data.HeatLogDao
import com.github.tntkhang.gmailsenderlibrary.GmailListener

import com.github.tntkhang.gmailsenderlibrary.GMailSender
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var heatingDao: HeatLogDao

    var handler = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null

    var isSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = HeatLogAdapter()
        binding.rvLogs.adapter = adapter

        heatingDao = AppDatabase.getInstance(this).heatLogDao()


        heatingDao.getAllLive().observe(this, Observer {
            Log.wtf("HEATING", it.size.toString())
            it?.let {
                adapter.data = it
            }
        })

        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Attensön")
                .setMessage("Tényleg törlöd?")
                .setIcon(R.drawable.ic_dialog_alert)
                .setPositiveButton(
                    R.string.yes,
                    DialogInterface.OnClickListener { dialog, whichButton ->
                        heatingDao.deleteAll()
                        Toast.makeText(
                            this@MainActivity,
                            "Törölve",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                .setNegativeButton(R.string.no, null).show()
        }

        binding.btnSend.setOnClickListener {
//            sendFile(heatingDao.getAll())
            // Write a message to the database
        }

        val intent = Intent(this, BackgroundService::class.java)
        startService(intent)


//        val runnable: Runnable = object : Runnable {
//            override fun run() {
//                // need to do tasks on the UI thread
//                val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
//                Log.wtf("TAAAAAAAAG", hour.toString())
//                if (hour == 6) {
//                    if (!isSent) {
//                        autoSenEmail()
//                        isSent = true
//                    }
//                } else {
//                    isSent = false
//                }
//
//                handler.postDelayed(this, 1000 * 60 * 15)
//            }
//        }

//        handler.postAtTime(runnable, 2000)

    }

    fun sendFile(list: List<HeatLog?>?) {
        val columnString = "Idő;Esemény"

        val dataString = getDataString(list)

        val combinedString = "$columnString\n$dataString"

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "CSV" + timestamp

        val storageDir = cacheDir
        val file = File.createTempFile(
            imageFileName,  /* prefix */
            ".csv",  /* suffix */
            storageDir /* directory */
        )

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            out!!.write(combinedString.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            out!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val u1 = FileProvider.getUriForFile(
            this@MainActivity,
            "com.example.futes.fileprovider",  //(use your app signature + ".provider" )
            file
        )

        var sendIntent = Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Futes csv");
        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
        sendIntent.setType("text/html");

        startActivity(sendIntent);
    }

    private fun getDataString(list: List<HeatLog?>?): String? {
        val dataString = list?.map {
            it?.let {
                getDateTime(it.timestamp) + ";" + it.event
            }
        }?.joinToString("\n")
        return dataString
    }

    private fun autoSenEmail() {
        GMailSender
            .withAccount("bolgar.david.93.work@gmail.com", "LmerdK5D")
            .withTitle("Futes")
            .withBody(getDataString(heatingDao.getAll()))
            .withSender("Futes")
            .toEmailAddress("dymer8@gmail.com") // one or multiple addresses separated by a comma
            .withListenner(object : GmailListener {
                override fun sendSuccess() {
                    Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                }

                override fun sendFail(err: String) {
                    Toast.makeText(this@MainActivity, "Fail: $err", Toast.LENGTH_SHORT).show()
                }
            })
            .send()
    }


    private fun getDateTime(timestamp: Long): String? {
        try {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

}