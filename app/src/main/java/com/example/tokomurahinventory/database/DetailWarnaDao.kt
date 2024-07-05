package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.MerkTable

@Dao
interface DetailWarnaDao {
    @Insert
    fun insert(detailWarnaTable: DetailWarnaTable)

    @Update
    fun update(detailWarnaTable: DetailWarnaTable)

    @Query("DELETE FROM detail_warna_table WHERE id=:id")
    fun deleteAnItemMerk(id:Int)

    @Query("SELECT * from detail_warna_table WHERE warnaRef =:warnaRef GROUP BY detailWarnaIsi")
    fun selectDetailWarnaByWarnaIdGroupByIsi(warnaRef:String):LiveData<List<DetailWarnaTable>>
}