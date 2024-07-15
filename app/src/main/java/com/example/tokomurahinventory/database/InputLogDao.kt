package com.example.tokomurahinventory.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tokomurahinventory.models.InputLogTable

@Dao
interface InputLogDao {
    @Insert
    fun insert(inputLogTable: InputLogTable)

   // @Query("")
    //fun selectListAsModel()
}