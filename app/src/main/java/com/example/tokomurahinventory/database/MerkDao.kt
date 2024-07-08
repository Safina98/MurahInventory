package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.MerkTable

@Dao
interface MerkDao  {
    @Insert
    fun insert(merkTable: MerkTable)

    @Update
    fun update(merkTable: MerkTable)

    @Query("DELETE FROM merk_table WHERE id=:id")
    fun deleteAnItemMerk(id:Int)

    @Query("SELECT * FROM merk_table")
    fun selectAllMerk():LiveData<List<MerkTable>>

    @Query("SELECT * FROM merk_table")
    fun selectAllMerkList():List<MerkTable>

    @Query("SELECT refMerk FROM merk_table WHERE namaMerk = :namaMerk")
    fun getMerkRefByName(namaMerk:String):String

    @Query("SELECT namaMerk FROM merk_table WHERE refMerk = :merkRef")
    fun getMerkNameByRef(merkRef:String):String







}