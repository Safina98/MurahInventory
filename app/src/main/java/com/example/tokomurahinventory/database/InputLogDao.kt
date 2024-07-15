package com.example.tokomurahinventory.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tokomurahinventory.models.InputLogTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.InputStokLogModel
import java.util.Date

@Dao
interface InputLogDao {
    @Insert
    fun insert(inputLogTable: InputLogTable)

    @Query("""
        SELECT 
            input_log_table.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            input_log_table.pcs,
            input_log_table.isi,
            input_log_table.barangLogInsertedDate,
            input_log_table.createdBy,
            input_log_table.inputBarangLogRef
        FROM 
            input_log_table
        JOIN 
            merk_table ON input_log_table.refMerk = merk_table.refMerk
        JOIN 
            warna_table ON input_log_table.warnaRef = warna_table.warnaRef
    """)
    fun getAllInputLogs(): List<InputStokLogModel>
    @Query("SELECT * FROM input_log_table")
    fun selectAllTable():List<InputLogTable>

    @Query("""
         SELECT 
            input_log_table.id,
            merk_table.namaMerk,
            warna_table.kodeWarna,
            warna_table.satuan,
            input_log_table.pcs,
            input_log_table.isi,
            input_log_table.barangLogInsertedDate,
            input_log_table.createdBy,
            input_log_table.inputBarangLogRef
        FROM 
            input_log_table
        JOIN 
            merk_table ON input_log_table.refMerk = merk_table.refMerk
        JOIN 
            warna_table ON input_log_table.warnaRef = warna_table.warnaRef
        WHERE (:startDate IS NULL OR barangLogInsertedDate >= :startDate)
        AND (:endDate IS NULL OR barangLogInsertedDate <= :endDate)
    """)
    fun getLogsByDateRange(startDate: Date?, endDate: Date?): List<InputStokLogModel>
}