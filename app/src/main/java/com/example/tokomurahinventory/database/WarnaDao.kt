package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable


@Dao
interface WarnaDao {
    @Insert
    fun insert(warnaTable: WarnaTable)

    @Update
    fun update(warnaTable: WarnaTable)

    @Query("DELETE FROM warna_table WHERE idWarna =:id")
    fun deleteAnItemWarna(id:Int)

    @Query("SELECT * FROM warna_table WHERE refMerk = :refMerk")
    fun selectWarnaByMerk(refMerk:String):LiveData<List<WarnaTable>>

    @Query("SELECT kodeWarna FROM warna_table WHERE warnaRef = :refWarna")
    fun selectWarnaByWarnaRef(refWarna:String):LiveData<String>

}