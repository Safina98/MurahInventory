package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "log_table",
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
    indices = [Index(value = ["refLog"], unique = true)])
data class LogTable(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    @ColumnInfo(name="userName")
    var userName:String="",
    @ColumnInfo(name="password")
    var password:String="",
    @ColumnInfo(name="namaToko")
    var namaToko:String="",
    @ColumnInfo(name="logDate")
    var logCreatedDate: Date =Date(),
    @ColumnInfo(name="keterangan")
    var keterangan:String="",
    @ColumnInfo(name="merk")
    var merk:String="",
    @ColumnInfo(name="kodeWarna")
    var kodeWarna:String="",
    @ColumnInfo(name="logIsi")
    var logIsi:Double=0.0,
    @ColumnInfo(name="logPcs")
    var pcs:Int=0,
    @ColumnInfo(name="detailWarnaRef")
    var detailWarnaRef:String="",
    @ColumnInfo(name="refLog")
    var refLog:String="",
    @ColumnInfo(name = "logLastEditedDate")
    var logLastEditedDate: Date = Date(),
    @ColumnInfo(name = "createdBy")
    var createdBy: String = "",
    @ColumnInfo(name = "lastEditedBy")
    var lastEditedBy: String = ""

)