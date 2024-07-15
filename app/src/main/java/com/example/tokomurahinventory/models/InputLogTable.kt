package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "input_log_table",
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
            entity = DetailWarnaTable::class,
            parentColumns = ["detailWarnaRef"],
            childColumns = ["detailWarnaRef"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsersTable::class,
            parentColumns = ["userName"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsersTable::class,
            parentColumns = ["userName"],
            childColumns = ["lastEditedBy"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["inputBarangLogRef"], unique = true)])
data class InputLogTable (
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
    @ColumnInfo(name="barangLogInsertedDate")
    var barangLogInsertedDate: Date = Date(),
    @ColumnInfo(name="barangLogLastEditedDate")
    var barangLogLastEditedDate: Date = Date(),
    // log_table
    @ColumnInfo(name="createdBy")
    var createdBy: String = "",
    @ColumnInfo(name="lastEditedBy")
    var lastEditedBy: String = "",
    @ColumnInfo(name="inputBarangLogRef")
    var inputBarangLogRef: String = ""
)