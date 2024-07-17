package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.model.CombinedDataModel
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import java.util.Date

@Dao
interface DetailWarnaDao {
    @Insert
    fun insert(detailWarnaTable: DetailWarnaTable)

    @Update
    fun update(detailWarnaTable: DetailWarnaTable)

    @Query("DELETE FROM detail_warna_table WHERE id=:id")
    fun deleteAnItemMerk(id:Int)

    @Query("DELETE FROM detail_warna_table WHERE warnaRef = :warnaRef and detailWarnaIsi =:isi")
    fun deteteDetailWarnaByIsi(warnaRef: String,isi: Double)

    @Query("SELECT * from detail_warna_table WHERE warnaRef =:warnaRef GROUP BY detailWarnaIsi")
    fun selectDetailWarnaByWarnaIdGroupByIsi(warnaRef:String):LiveData<List<DetailWarnaTable>>


    @Query("""SELECT * FROM detail_warna_table WHERE detailWarnaIsi = :detailWarnaIsi AND detailWarnaRef = :warnaRef LIMIT :pcs""")
    fun getFirstDetailWarna(detailWarnaIsi: Double, warnaRef: String,pcs:Int): List<DetailWarnaTable>

    @Query("""SELECT * FROM detail_warna_table """)
    fun getAllDetailWarnas(): List<DetailWarnaTable>

    @Query("""
        SELECT 
            d.detailWarnaIsi,
            d.warnaRef,
            w.satuan,
            SUM(d.detailWarnaPcs) as detailWarnaPcs
        FROM detail_warna_table d
        INNER JOIN warna_table w ON d.warnaRef = w.warnaRef
        WHERE d.warnaRef = :warnaRef 
        GROUP BY d.detailWarnaIsi, d.warnaRef, w.satuan
    """)
    fun getDetailWarnaSummary(warnaRef: String): LiveData<List<DetailWarnaModel>>

    @Query(" UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs-:detailWarnaPcs WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    fun updateDetailWarna(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int): Int

    @Query("""
        UPDATE detail_warna_table 
        SET detailWarnaPcs = detailWarnaPcs + :detailWarnaPcs, 
            lastEditedBy = :lastEditedBy, 
            detailWarnaLastEditedDate = :lastEditedDate 
        WHERE warnaRef = :refWarna 
        AND detailWarnaIsi = :detailWarnaIsi
    """)
    fun updateDetailWarnaA(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,lastEditedBy:String,lastEditedDate:Date): Int

    @Query("""
        UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs+:detailWarnaPcs WHERE detailWarnaRef = :refDetailWarna AND detailWarnaIsi = :detailWarnaIsi;
""")
    fun updateOldDetailWarna(refDetailWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int
    )

    @Query("SELECT * FROM detail_warna_table WHERE detailWarnaIsi = :isi AND detailWarnaRef = :warnaRef")
    fun getDetailWarnaByIsiAndRef(isi: Double, warnaRef: String): List<DetailWarnaTable>

    @Query("""
        SELECT 
            d.detailWarnaIsi
        FROM detail_warna_table d
        WHERE d.warnaRef = :warnaRef
    """)
    fun getIsiDetailWarnaByWarna(warnaRef: String): List<Double>


    @Query("SELECT detailWarnaRef FROM detail_warna_table WHERE warnaRef = :warnaRef and detailWarnaIsi =:isi")
    fun getDetailWarnaByIsi(warnaRef: String,isi: Double):String


    @Query("SELECT * from detail_warna_table WHERE warnaRef =:warnaRef")
    fun selecttTry(warnaRef:String):List<DetailWarnaTable>

    @Query("SELECT * FROM detail_warna_table WHERE detailWarnaIsi = :detailWarnaIsi AND warnaRef = :warnaRef LIMIT 1")
    fun checkIfIsiExisted(detailWarnaIsi: Double, warnaRef: String): DetailWarnaTable?

    @Query("""
        SELECT 
            m.id AS merkId,
            m.namaMerk,
            m.refMerk,
            m.merkCreatedDate,
            m.merkLastEditedDate,
            m.createdBy AS merkCreatedBy,
            m.lastEditedBy AS merkLastEditedBy,
            w.idWarna AS warnaId,
            w.kodeWarna,
            w.totalPcs,
            w.satuanTotal,
            w.satuan,
            w.warnaRef,
            w.warnaCreatedDate,
            w.warnaLastEditedDate,
            w.createdBy AS warnaCreatedBy,
            w.lastEditedBy AS warnaLastEditedBy,
            d.id AS detailWarnaId,
            d.detailWarnaIsi,
            d.detailWarnaPcs,
            d.detailWarnaDate,
            d.detailWarnaLastEditedDate,
            d.createdBy AS detailWarnaCreatedBy,
            d.lastEditedBy AS detailWarnaLastEditedBy
        FROM merk_table AS m
        JOIN warna_table AS w ON m.refMerk = w.refMerk
        JOIN detail_warna_table AS d ON w.warnaRef = d.warnaRef
    """)
    fun getAllCombinedData(): List<CombinedDataModel>
}