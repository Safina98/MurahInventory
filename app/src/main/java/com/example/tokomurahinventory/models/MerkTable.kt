package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "merk_table",
    foreignKeys = [
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
    indices = [Index(value = ["refMerk"], unique = true)])
data class MerkTable (
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    @ColumnInfo(name="namaMerk")
    var namaMerk:String="",
    @ColumnInfo(name="refMerk")
    var refMerk:String="",
    @ColumnInfo(name = "merkCreatedDate")
    var merkCreatedDate: Date = Date(),
    @ColumnInfo(name = "merkLastEditedDate")
    var merkLastEditedDate: Date = Date(),
    @ColumnInfo(name = "createdBy")
    var createdBy: String = "",
    @ColumnInfo(name = "lastEditedBy")
    var lastEditedBy: String = ""
)