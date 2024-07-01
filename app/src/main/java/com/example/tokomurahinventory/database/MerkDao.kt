package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tokomurahinventory.models.MerkTable

@Dao
interface MerkDao  {
    @Insert
    fun insert(merkTable: MerkTable)

    @Query("SELECT * FROM merk_table")
    fun selectAllMerk():LiveData<List<MerkTable>>
}