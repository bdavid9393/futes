package com.example.futes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeatLog(
    @PrimaryKey val id: Long,
    val timestamp: Long,
    val event: String
)