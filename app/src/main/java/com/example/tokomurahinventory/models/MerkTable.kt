package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merk_table")
data class MerkTable (
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @ColumnInfo(name="namaMerk")
    var namaMerk:String="",
    @ColumnInfo(name="refMerk")
    var ref:String=""
)