package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "barang_log",
    indices = [Index(value = ["barangLogRef"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = WarnaTable::class,
            parentColumns = ["warnaRef"],
            childColumns = ["warnaRef"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MerkTable::class,
            parentColumns = ["refMerk"],
            childColumns = ["refMerk"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LogTable::class,
            parentColumns = ["refLog"],
            childColumns = ["refLog"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DetailWarnaTable::class,
            parentColumns = ["detailWarnaRef"],
            childColumns = ["detailWarnaRef"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class BarangLog(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    // merk_table refMerk
    @ColumnInfo(name="refMerk")
    var refMerk: String = "",
    // warna_table warnaRef
    @ColumnInfo(name="warnaRef")
    var warnaRef: String = "",
    // detail_warna_table,
    @ColumnInfo(name="detailWarnaRef")
    var detailWarnaRef: String = "",
    @ColumnInfo(name="isi")
    var isi: Double = 0.0,
    // detail_warna_table
    @ColumnInfo(name="pcs")
    var pcs: Int = 0,
    @ColumnInfo(name="barangLogDate")
    var barangLogDate: Date = Date(),
    // log_table
    @ColumnInfo(name="refLog")
    var refLog: String = "",
    @ColumnInfo(name="barangLogRef")
    var barangLogRef: String = ""
)
