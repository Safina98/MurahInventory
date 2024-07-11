package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "warna_table",
    indices = [Index(value = ["warnaRef"], unique = true)],
    foreignKeys = [
        ForeignKey(entity = MerkTable::class,
        parentColumns = ["refMerk"],
        childColumns = ["refMerk"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE),
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

    ])
data class WarnaTable (
    @PrimaryKey(autoGenerate = true)
    var idWarna:Int=0,
    @ColumnInfo(name="refMerk")
    var refMerk:String="",
    @ColumnInfo(name="kodeWarna")
    var kodeWarna:String="",
    @ColumnInfo(name="totalPcs")
    var totalPcs:Int=0,
    @ColumnInfo(name="satuanTotal")
    var satuanTotal:Double=0.0,
    @ColumnInfo(name="satuan")
    var satuan:String="",
    @ColumnInfo(name="warnaRef")
    var warnaRef:String="",
    @ColumnInfo(name = "warnaCreatedDate")
    var warnaCreatedDate: Date = Date(),
    @ColumnInfo(name = "warnaLastEditedDate")
    var warnaLastEditedDate: Date = Date(),
    @ColumnInfo(name = "createdBy")
    var createdBy: String = "",
    @ColumnInfo(name = "lastEditedBy")
    var lastEditedBy: String = ""
)