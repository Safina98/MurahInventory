package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.CombinedLogData
import java.util.Date

@Dao
interface LogDao {
    @Insert
    fun insert(logTable: LogTable)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLogTable(logTable: LogTable)

    @Update
    fun update(logTable: LogTable)

    @Query("SELECT * FROM LOG_TABLE")
    fun selectAllLog():LiveData<List<LogTable>>

    @Query("SELECT * FROM LOG_TABLE WHERE refLog = :logRef")
    fun getLogById(logRef:String):LogTable

    @Query("SELECT * FROM LOG_TABLE WHERE logTipe =:tipe AND (:startDate IS NULL OR logDate >= :startDate) AND (:endDate IS NULL OR logDate <= :endDate) ")
    fun selectAllLogList(tipe:String,startDate: Date?, endDate: Date?):List<LogTable>

    @Query("DELETE FROM log_table WHERE id =:id")
    fun delete(id:Int)

    @Query("""
        SELECT * FROM log_table
        WHERE (:startDate IS NULL OR logDate >= :startDate)
        AND (:endDate IS NULL OR logDate <= :endDate) AND logTipe =:tipe
    """)
    fun getLogsByDateRange(startDate: Date?, endDate: Date?,tipe:String): List<LogTable>

    @Query("""
        SELECT 
            l.id AS logId,
            l.userName,
            l.password,
            l.namaToko,
            l.logDate AS logDate, -- Use the correct column name here
            l.keterangan,
            l.merk,
            l.kodeWarna,
            l.logIsi,
            l.logPcs AS logPcs,
            b.detailWarnaRef,
            l.refLog,
            l.logLastEditedDate,
            l.createdBy,
            l.lastEditedBy,
            l.logExtraBool,
            l.logExtraDouble,
            l.logExtraString,
            l.logTipe,
            b.id AS barangLogId,
            b.refMerk,
            b.warnaRef,
            b.isi AS barangLogIsi,
            b.pcs AS barangLogPcs,
            b.barangLogDate,
            b.barangLogRef,
            b.barangLogExtraBool,
            b.barangLogExtraDouble,
            b.barangLogExtraString,
            b.barangLogTipe
        FROM log_table AS l
        JOIN barang_log AS b ON l.refLog = b.refLog
    """)
    fun getAllCombinedLogData(): List<CombinedLogData>
    @Query(" UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs-:detailWarnaPcs,lastEditedBy =:loggedInUsers WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    fun updateDetailWarna(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,loggedInUsers:String?): Int
    @Query("DELETE FROM log_table WHERE logDate < :date")
    fun deleteLogsBefore(date: Date)
    @Transaction
    suspend fun deleteLogAndUpdateDetailWarna(
        log: LogTable,
        barangLogList: List<BarangLog>,
        loggedInUsers: String?
    ) {
        // Update detail_warna_table for each BarangLog
        barangLogList.forEach { barangLog ->
            updateDetailWarna(
                barangLog.warnaRef,
                barangLog.isi,
                barangLog.pcs * -1,
                loggedInUsers
            )
        }
        // Delete the LogTable entry
        delete(log.id)
    }
}