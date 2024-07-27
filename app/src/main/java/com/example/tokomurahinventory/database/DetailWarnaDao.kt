package com.example.tokomurahinventory.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.model.CombinedDataModel
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import java.util.Date

@Dao
interface DetailWarnaDao {
    @Insert
    fun insert(detailWarnaTable: DetailWarnaTable)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDetailWarnaTable(detailWarnaTable: DetailWarnaTable)

    @Update
    fun update(detailWarnaTable: DetailWarnaTable)

    @Query("DELETE FROM detail_warna_table WHERE id=:id")
    fun deleteAnItemDetailWarna(id:Int)

    @Query("DELETE FROM detail_warna_table WHERE warnaRef = :warnaRef and detailWarnaIsi =:isi")
    fun deteteDetailWarnaByIsi(warnaRef: String,isi: Double)

    @Query("SELECT * from detail_warna_table WHERE warnaRef =:warnaRef GROUP BY detailWarnaIsi")
    fun selectDetailWarnaByWarnaIdGroupByIsi(warnaRef:String):LiveData<List<DetailWarnaTable>>


    @Query("""SELECT * FROM detail_warna_table WHERE detailWarnaRef = :detailWarnaRef""")
    fun getDetailWarnaByDetailWarnaRef(detailWarnaRef: String):DetailWarnaTable

    @Query("""SELECT * FROM detail_warna_table """)
    fun getAllDetailWarnas(): List<DetailWarnaTable>

    @Query("""
        SELECT 
            d.detailWarnaIsi,
            d.warnaRef,
            w.satuan,
            d.createdBy,
            d.lastEditedBy,
            d.detailWarnaDate,
            d.detailWarnaLastEditedDate,
            d.user,
            SUM(d.detailWarnaPcs) as detailWarnaPcs
        FROM detail_warna_table d
        INNER JOIN warna_table w ON d.warnaRef = w.warnaRef
        WHERE d.warnaRef = :warnaRef 
        GROUP BY d.detailWarnaIsi, d.warnaRef, w.satuan
    """)
    fun getDetailWarnaSummary(warnaRef: String): LiveData<List<DetailWarnaModel>>
    @Query("""
        SELECT 
            d.detailWarnaIsi,
            d.warnaRef,
            w.satuan,
            d.createdBy,
            d.lastEditedBy,
            d.detailWarnaDate,
            d.detailWarnaLastEditedDate,
            d.user,
            SUM(d.detailWarnaPcs) as detailWarnaPcs
        FROM detail_warna_table d
        INNER JOIN warna_table w ON d.warnaRef = w.warnaRef
        WHERE d.warnaRef = :warnaRef 
        GROUP BY d.detailWarnaIsi, d.warnaRef, w.satuan
    """)
    fun getDetailWarnaSummaryList(warnaRef: String): List<DetailWarnaModel>

    @Query(" UPDATE detail_warna_table SET detailWarnaPcs = detailWarnaPcs-:detailWarnaPcs,lastEditedBy =:loggedInUsers WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    fun updateDetailWarna(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,loggedInUsers:String?): Int

    @Query("SELECT COUNT(*) FROM detail_warna_table WHERE warnaRef = :refWarna AND detailWarnaIsi = :detailWarnaIsi")
    suspend fun exists(refWarna: String, detailWarnaIsi: Double): Int

    @Query("""
        UPDATE detail_warna_table 
        SET detailWarnaPcs = detailWarnaPcs + :detailWarnaPcs, 
            lastEditedBy = :lastEditedBy, 
            detailWarnaLastEditedDate = :lastEditedDate 
        WHERE warnaRef = :refWarna 
        AND detailWarnaIsi = :detailWarnaIsi
    """)
    fun updateDetailWarnaA(refWarna:String, detailWarnaIsi: Double, detailWarnaPcs:Int,lastEditedBy:String,lastEditedDate:Date): Int


    @Query("SELECT * FROM detail_warna_table WHERE detailWarnaIsi = :isi AND detailWarnaRef = :detailWarnaRef")
    fun getDetailWarnaByIsiAndRef(isi: Double, detailWarnaRef: String): List<DetailWarnaTable>

    @Query("""SELECT d.detailWarnaIsi FROM detail_warna_table d WHERE d.warnaRef = :warnaRef""")
    fun getIsiDetailWarnaByWarna(warnaRef: String): List<Double>

    @Query("SELECT detailWarnaRef FROM detail_warna_table WHERE warnaRef = :warnaRef and detailWarnaIsi =:isi")
    fun getDetailWarnaRefByIsiAndWarnaRef(warnaRef: String, isi: Double):String?

    @Query("SELECT * FROM detail_warna_table WHERE warnaRef = :warnaRef and detailWarnaIsi =:isi")
    fun getDetailWarnaByIsii(warnaRef: String,isi: Double):DetailWarnaTable

    @Query("SELECT * from detail_warna_table WHERE warnaRef =:warnaRef")
    fun selecttTry(warnaRef:String):List<DetailWarnaTable>

    @Query("SELECT * from detail_warna_table")
    fun selectAll():List<DetailWarnaTable>

    @Query("SELECT * from detail_warna_table WHERE detailWarnaRef =:detailWarnaRef")
    fun selecttDetailWarnaByRef(detailWarnaRef:String):List<DetailWarnaTable>

    @Query("SELECT * FROM detail_warna_table WHERE detailWarnaIsi = :detailWarnaIsi AND warnaRef = :warnaRef LIMIT 1")
    fun checkIfIsiExisted(detailWarnaIsi: Double, warnaRef: String): DetailWarnaTable?

    @Query("SELECT EXISTS(SELECT 1 FROM detail_warna_table WHERE detailWarnaRef = :refDetailWarna AND detailWarnaPcs >= :pcs_n)")
    fun isPcsReady(refDetailWarna: String, pcs_n: Int): Boolean

    @Query("""
        SELECT 
            m.id AS merkId,
            m.namaMerk,
            m.refMerk,
            m.merkCreatedDate,
            m.merkLastEditedDate,
            m.user AS merkUser,
            m.createdBy AS merkCreatedBy,
            m.lastEditedBy AS merkLastEditedBy,
            w.idWarna AS warnaId,
            w.kodeWarna,
            w.totalPcs,
            w.satuanTotal,
            w.satuan,
            w.warnaRef,
            w.user AS warnaUser,
            w.warnaCreatedDate,
            w.warnaLastEditedDate,
            w.createdBy AS warnaCreatedBy,
            w.lastEditedBy AS warnaLastEditedBy,
            d.id AS detailWarnaId,
            d.detailWarnaIsi,
            d.detailWarnaPcs,
            d.detailWarnaDate,
            d.detailWarnaLastEditedDate,
            d.user AS detailWarnaUser,
            d.createdBy AS detailWarnaCreatedBy,
            d.lastEditedBy AS detailWarnaLastEditedBy,
            d.detailWarnaRef
        FROM detail_warna_table AS d
        JOIN warna_table AS w ON d.warnaRef = w.warnaRef
        JOIN merk_table AS m ON w.refMerk = m.refMerk
    """)
    fun getAllCombinedData(): List<CombinedDataModel>
}