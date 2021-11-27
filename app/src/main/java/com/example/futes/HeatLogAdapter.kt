package com.example.futes

import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.futes.data.HeatLog
import com.example.futes.databinding.ListItemHeatingBinding
import java.lang.String.format
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Locale


class HeatLogAdapter : RecyclerView.Adapter<HeatLogAdapter.ViewHolder>() {
    override fun getItemCount() = data.size

    var data = listOf<HeatLog>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListItemHeatingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HeatLog) {
            binding.tvDate.text = getDateTime(item.timestamp)
            binding.tvState.text = item.event
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    ListItemHeatingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
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
}