package com.example.futes.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface HeatLogDao {
    @Insert
    fun insertAll(vararg users: HeatLog)

    @Insert(onConflict = REPLACE)
    fun insert(heatLog: HeatLog)

    @Query("DELETE FROM HeatLog")
    fun deleteAll()

    @Query("SELECT * FROM HeatLog")
    fun getAllLive(): LiveData<List<HeatLog>>

    @Query("SELECT * FROM HeatLog")
    fun getAll(): List<HeatLog>
}