package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.models.model.WarnaModel


@Dao
interface WarnaDao {
    @Insert
    fun insert(warnaTable: WarnaTable)

    @Update
    fun update(warnaTable: WarnaTable)

    @Query("DELETE FROM warna_table WHERE idWarna =:id")
    fun deleteAnItemWarna(id:Int)

    //old rv
    @Query("SELECT * FROM warna_table WHERE refMerk = :refMerk")
    fun selectWarnaByMerk(refMerk:String):LiveData<List<WarnaTable>>

    @Query("SELECT kodeWarna FROM warna_table WHERE warnaRef = :refWarna")
    fun selectWarnaByWarnaRef(refWarna:String):LiveData<String>

    @Query("SELECT warnaRef FROM warna_table WHERE refMerk=:refMerk AND kodeWarna = :namaWarna")
    fun getWarnaRefByName(namaWarna:String, refMerk: String):String

    @Query("SELECT kodeWarna FROM warna_table WHERE warnaRef = :warnaRef")
    fun getKodeWarnaByRef(warnaRef:String):String


        @Query("""
        SELECT 
            w.idWarna,
            w.refMerk,
            w.kodeWarna,
            w.totalPcs,
            w.satuanTotal,
            w.satuan,
            w.warnaRef,
            SUM(d.detailWarnaPcs) as totalDetailPcs
        FROM warna_table w
        LEFT JOIN detail_warna_table d ON w.warnaRef = d.warnaRef
        where w.refMerk = :refMerk
        GROUP BY w.idWarna, w.refMerk, w.kodeWarna, w.totalPcs, w.satuanTotal, w.satuan, w.warnaRef
    """)
        fun getWarnaWithTotalPcs(refMerk:String): LiveData<List<WarnaModel>>

    @Query("""
        SELECT 
            w.idWarna,
            w.refMerk,
            w.kodeWarna,
            w.totalPcs,
            w.satuanTotal,
            w.satuan,
            w.warnaRef,
            SUM(d.detailWarnaPcs) as totalDetailPcs
        FROM warna_table w
        LEFT JOIN detail_warna_table d ON w.warnaRef = d.warnaRef
        where w.refMerk = :refMerk
        GROUP BY w.idWarna, w.refMerk, w.kodeWarna, w.totalPcs, w.satuanTotal, w.satuan, w.warnaRef
    """)
    fun getWarnaWithTotalPcsList(refMerk:String): List<WarnaModel>



}