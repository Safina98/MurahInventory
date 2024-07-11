package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.LogTable
import java.util.Date

@Dao
interface LogDao {
    @Insert
    fun insert(logTable: LogTable)

    @Update
    fun update(logTable: LogTable)

    @Query("SELECT * FROM LOG_TABLE")
    fun selectAllLog():LiveData<List<LogTable>>

    @Query("SELECT * FROM LOG_TABLE")
    fun selectAllLogList():List<LogTable>

    @Query("DELETE FROM log_table WHERE id =:id")
    fun delete(id:Int)

    @Query("""
        SELECT * FROM log_table
        WHERE (:startDate IS NULL OR logDate >= :startDate)
        AND (:endDate IS NULL OR logDate <= :endDate)
    """)
    fun getLogsByDateRange(startDate: Date?, endDate: Date?): List<LogTable>
}