package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.LogTable
import java.util.Date


@Dao
interface BarangLogDao {
    @Insert
    fun insert(barangLog: BarangLog)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBarangLogTable(barangLog: BarangLog)

    @Update
    fun update(barangLog: BarangLog)

    @Query("DELETE FROM barang_log WHERE id =:id")
    fun delete(id:Int)

    @Query("SELECT * FROM barang_log")
    fun selectAllLog(): LiveData<List<BarangLog>>

    @Query("SELECT * FROM barang_log WHERE refLog = :refLog")
    fun selectBarangLogByLogRef(refLog:String): List<BarangLog>

    @Query("SELECT * FROM barang_log WHERE barangLogRef = :ref")
    fun selectBarangLogByRef(ref:String): BarangLog


    @Query("UPDATE barang_log SET refMerk = :refMerk, warnaRef = :warnaRef, detailWarnaRef = :detailWarnaRef, isi = :isi, pcs = :pcs, barangLogDate = :barangLogDate, refLog = :refLog WHERE barangLogRef = :barangLogRef")
    fun updateByBarangLogRef(
        refMerk: String,
        warnaRef: String,
        detailWarnaRef: String,
        isi: Double,
        pcs: Int,
        barangLogDate: Date,
        refLog: String,
        barangLogRef: String
    )
}