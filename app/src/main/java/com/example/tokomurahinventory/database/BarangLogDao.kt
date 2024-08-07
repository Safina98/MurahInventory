package com.example.tokomurahinventory.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.InputStokLogModel
import java.util.Date
import kotlin.math.log


@Dao
interface BarangLogDao {
    @Insert
    fun insert(barangLog: BarangLog):Long

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

    @Query("SELECT detailWarnaPcs FROM detail_warna_table WHERE warnaRef = :warnaRef AND detailWarnaIsi = :detailWarnaIsi")
    suspend fun getCurrentDetailWarnaPcs(warnaRef: String, detailWarnaIsi: Double): Int


    @Query("SELECT * FROM barang_log")
    fun selectAllLog(): LiveData<List<BarangLog>>

    @Query("SELECT * FROM barang_log WHERE refLog = :refLog")
    fun selectBarangLogByLogRef(refLog:String): List<BarangLog>

    @Query("UPDATE barang_log SET refMerk = :refMerk, warnaRef = :warnaRef, detailWarnaRef = :detailWarnaRef, isi = :isi, pcs = :pcs, barangLogDate = :barangLogDate, refLog = :refLog WHERE barangLogRef = :barangLogRef")
    fun updateByBarangLogRef(refMerk: String, warnaRef: String, detailWarnaRef: String, isi: Double, pcs: Int, barangLogDate: Date, refLog: String, barangLogRef: String):Int

    @Query("SELECT * FROM barang_log WHERE barangLogRef =:barangLogRef")
    fun findByBarangLogRef(barangLogRef:String):BarangLog?

    @Query("""
        SELECT 
            barang_log.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            barang_log.pcs,
            barang_log.isi,
            log_table.lastEditedBy AS barangLogInsertedDate,
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
        AND (:startDate IS NULL OR log_table.logDate >= :startDate)
        AND (:endDate IS NULL OR log_table.logDate <= :endDate)
    """)
    fun getAllLogMasuk(tipe:String,startDate: Date?, endDate: Date?): List<InputStokLogModel>

    @Query("""
        SELECT 
            barang_log.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            barang_log.pcs,
            barang_log.isi,
            log_table.logLastEditedDate AS barangLogInsertedDate,
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
        AND (:startDate IS NULL OR log_table.logLastEditedDate >= :startDate)
        AND (:endDate IS NULL OR log_table.logLastEditedDate <= :endDate)
    """)
    fun getAllLogMasukLiveData(tipe:String,startDate: Date?, endDate: Date?): LiveData<List<InputStokLogModel>>

    @Query("""
         SELECT 
            barang_log.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            barang_log.pcs,
            barang_log.isi,
            log_table.logLastEditedDate AS barangLogInsertedDate,
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
        WHERE (:startDate IS NULL OR log_table.logLastEditedDate >= :startDate)
        AND (:endDate IS NULL OR log_table.logLastEditedDate <= :endDate AND log_table.logTipe =:tipe)
    """)
    fun getLogMasukByDateRange(startDate: Date?, endDate: Date?, tipe:String): List<InputStokLogModel>

    @Query("""
        UPDATE log_table SET lastEditedBy = :lastEditedBy, logLastEditedDate = :logLastEditedDate, userName = :userName WHERE refLog = :refLog
    """)
    suspend fun updateLastEditedLog(
        lastEditedBy: String?,
        logLastEditedDate: Date,
        userName: String,
        refLog: String
    )
    @Query(" UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs-:detailWarnaPcs,lastEditedBy =:loggedInUsers,detailWarnaLastEditedDate=:lastEditedDate WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    fun updateDetailWarna(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,loggedInUsers:String?,lastEditedDate: Date): Int
    @Query(" UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs-:detailWarnaPcs,lastEditedBy =:loggedInUsers,detailWarnaLastEditedDate=:lastEditedDate,detailWarnaKet=:ket WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    fun updateDetailWarnaWithKet(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,loggedInUsers:String?,lastEditedDate: Date,ket:String): Int
    @Insert
    fun insert(detailWarnaTable: DetailWarnaTable)
    @Insert
    fun insert(logTable: LogTable)
    @Query("""
        UPDATE detail_warna_table 
        SET detailWarnaPcs = detailWarnaPcs + :detailWarnaPcs, 
            lastEditedBy = :lastEditedBy, 
            detailWarnaLastEditedDate = :lastEditedDate ,
            detailWarnaKet=:ket
        WHERE warnaRef = :refWarna 
        AND detailWarnaIsi = :detailWarnaIsi
    """)
    fun updateDetailWarnaA(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,lastEditedBy:String?,lastEditedDate:Date,ket:String): Int


    @Transaction
    suspend fun updateDetailAndDeleteBarangLog(
        refWarna: String,
        detailWarnaIsi: Double,
        detailWarnaPcs: Int,
        loggedInUsers: String?,
        id: Int,
        detailWarnaKet:String
    ) {
        // Retrieve the current pcs value for the given refWarna and detailWarnaIsi
        val currentPcs = getCurrentDetailWarnaPcs(refWarna, detailWarnaIsi)

        // Calculate the new pcs value after the update
        val newPcs = currentPcs - detailWarnaPcs

        // Check if the new pcs value would be negative
        if (newPcs >= 0) {
            // Perform the update
           // updateDetailWarna(refWarna, detailWarnaIsi, detailWarnaPcs, loggedInUsers, Date())
            updateDetailWarnaWithKet(refWarna, detailWarnaIsi, detailWarnaPcs, loggedInUsers, Date(),detailWarnaKet)
            // Perform the delete operation
            delete(id)
        } else {
            // Log the error and throw an exception to roll back the transaction
            Log.e("InsertLogTry", "Update failed: Negative pcs value detected for refWarna $refWarna and detailWarnaIsi $detailWarnaIsi")
            throw IllegalArgumentException("Stok tidak cukup, stok sisa $currentPcs")
        }
    }

    @Transaction
    suspend fun updateBarangLogAndDetails(
        refMerk: String,
        warnaRef: String,
        detailWarnaRef: String,
        isi: Double,
        pcs: Int,
        barangLogDate: Date,
        refLog: String,
        barangLogRef: String,
        detailWarnaUpdates: List<DetailWarnaTable>,
        loggedInUsers: String?
    ) {
        Log.e("InsertLogTry", "4 BarangLogDate ${barangLogDate}")
        updateLastEditedLog(loggedInUsers,Date(),loggedInUsers?:"",refLog)
        // Update BarangLog Table
        updateByBarangLogRef(refMerk, warnaRef, detailWarnaRef, isi, pcs, barangLogDate, refLog, barangLogRef)
        // Perform DetailWarnaTable updates
        detailWarnaUpdates.forEach { update ->
            val currentPcs = getCurrentDetailWarnaPcs(update.warnaRef, update.detailWarnaIsi)
            val newPcs = currentPcs - update.detailWarnaPcs

            if (newPcs >= 0) {
                updateDetailWarnaWithKet(update.warnaRef, update.detailWarnaIsi, update.detailWarnaPcs, loggedInUsers, Date(),update.detailWarnaKet?:"")
            } else {
                // Log the error and throw an exception to roll back the transaction
                Log.e("InsertLogTry", "Update failed: Negative pcs value detected for warnaRef ${update.warnaRef} and detailWarnaIsi ${update.detailWarnaIsi}")
                throw IllegalArgumentException("Stok tidak cukup, stok sisa $currentPcs")
            }
        }
    }

    @Transaction
    suspend fun insertBarangLogAndUpdateDetailWarna(
        barangLog: BarangLog,
        refWarna: String,
        detailWarnaIsi: Double,
        detailWarnaPcs: Int,
        loggedInUsers: String?,
        ket:String
    ) {
        // Insert the BarangLog and get the generated ID (if needed)
        val barangLogId = insert(barangLog)

        // Update one entry in detail_warna_table
        updateDetailWarnaWithKet(refWarna, detailWarnaIsi, detailWarnaPcs, loggedInUsers,Date(),ket)
    }

    @Transaction
    suspend fun insertLogAndUpdateDetailWarna(
        logTable: LogTable,
        barangLogs: List<BarangLog>,
        loggedInUsers: String?,

    ) {
        // Insert the LogTable entry
        insert(logTable)
        // Insert BarangLog entries and update detail_warna_table
        barangLogs.forEach { barangLog ->
            // Insert BarangLog
            insert(barangLog)
            val ket =  "Barang keluar sebanyak ${barangLog.pcs} pcs, ke toko ${logTable.namaToko}"
            // Update detail_warna_table
            updateDetailWarnaWithKet(
                barangLog.warnaRef,
                barangLog.isi,
                barangLog.pcs,
                loggedInUsers,
                Date(),
                ket
            )
        }
    }

    @Transaction
    suspend fun updateBarangLogAndDetailWarna(
        newBarangLog: BarangLog,
        oldBarangLog: BarangLog?,
        loggedInUsers: String?,
        ket: String
    ) {
        Log.i("DAO", "Updating barang log with ref: ${newBarangLog.barangLogRef}")
        // Update barang log
        updateByBarangLogRef(
            newBarangLog.refMerk,
            newBarangLog.warnaRef,
            newBarangLog.detailWarnaRef ?: "",
            newBarangLog.isi,
            newBarangLog.pcs,
            newBarangLog.barangLogDate,
            newBarangLog.refLog,
            newBarangLog.barangLogRef
        )

        // Update detail warna
        oldBarangLog?.let {
            Log.i("DAO", "Updating detail warna for old barang log: ${it.warnaRef}")
            updateDetailWarna(it.warnaRef, it.isi, it.pcs, loggedInUsers,Date())
        }
        Log.i("DAO", "Updating detail warna for new barang log: ${newBarangLog.warnaRef}")
        updateDetailWarna(newBarangLog.warnaRef, newBarangLog.isi, -newBarangLog.pcs, loggedInUsers,Date())
    }

    @Transaction
    suspend fun performUpdateDetailWarnaAndInsertLogAndBarangLogFromDetailWarna(
        refWarna: String,
        detailWarnaIsi: Double,
        detailWarnaPcs: Int,
        lastEditedBy: String?,
        lastEditedDate: Date,
        log: LogTable,
        barangLog: BarangLog,
        ket: String
    ) {
        // Update the detail_warna_table
        updateDetailWarnaA(refWarna, detailWarnaIsi, detailWarnaPcs, lastEditedBy, lastEditedDate,ket)
        // Insert into LogTable
        insert(log)
        // Insert into BarangLog and get the new ID
        insert(barangLog)
    }
    @Transaction
    suspend fun insertDetailWarnaAndLogAndBarangLogFromDetailWarna(
        detailWarnaTable: DetailWarnaTable,
        log: LogTable,
        barangLog: BarangLog
    ) {
        // Update the detail_warna_table
        insert(detailWarnaTable)
        // Insert into LogTable
        insert(log)
        // Insert into BarangLog and get the new ID
        insert(barangLog)
    }
}