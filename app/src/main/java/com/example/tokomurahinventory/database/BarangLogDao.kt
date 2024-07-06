package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tokomurahinventory.models.BarangLog


@Dao
interface BarangLogDao {
    @Insert
    fun insert(barangLog: BarangLog)

    @Query("SELECT * FROM barang_log")
    fun selectAllLog(): LiveData<List<BarangLog>>
}