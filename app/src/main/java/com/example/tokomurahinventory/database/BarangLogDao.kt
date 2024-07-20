package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.InputStokLogModel
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

    @Query("SELECT merk_table.namaMerk AS merkBarang, warna_table.kodeWarna AS kodeBarang,barang_log.id as id, barang_log.isi, barang_log.pcs as psc, barang_log.refLog as logRef, barang_log.barangLogRef " +
            "FROM barang_log " +
            "JOIN merk_table ON barang_log.refMerk = merk_table.refMerk " +
            "JOIN warna_table ON barang_log.warnaRef = warna_table.warnaRef " +
            "WHERE barang_log.refLog = :refLog")
    fun selectCountModelByLogRef(refLog: String): List<CountModel>

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

    @Query("SELECT * FROM barang_log WHERE barangLogRef =:barangLogRef")
    fun findByBarangLogRef(barangLogRef:String):BarangLog


    @Query("""
        SELECT 
            barang_log.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            barang_log.pcs,
            barang_log.isi,
            log_table.logDate AS barangLogInsertedDate,
            log_table.createdBy,
            barang_log.barangLogRef AS inputBarangLogRef
        FROM 
            barang_log
        JOIN 
            log_table ON barang_log.refLog = log_table.refLog
        JOIN 
            merk_table ON barang_log.refMerk = merk_table.refMerk
        JOIN 
            warna_table ON barang_log.warnaRef = warna_table.warnaRef
        WHERE 
        barangLogTipe =:tipe
    """)
    fun getAllLogMasuk(tipe:String): List<InputStokLogModel>

    @Query("""
         SELECT 
            barang_log.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            barang_log.pcs,
            barang_log.isi,
            log_table.logDate AS barangLogInsertedDate,
            log_table.createdBy,
            barang_log.barangLogRef AS inputBarangLogRef
        FROM 
            barang_log
        JOIN 
            log_table ON barang_log.refLog = log_table.refLog
        JOIN 
            merk_table ON barang_log.refMerk = merk_table.refMerk
        JOIN 
            warna_table ON barang_log.warnaRef = warna_table.warnaRef
        WHERE (:startDate IS NULL OR log_table.logDate >= :startDate)
        AND (:endDate IS NULL OR log_table.logDate <= :endDate)
    """)
    fun getLogMasukByDateRange(startDate: Date?, endDate: Date?): List<InputStokLogModel>
}