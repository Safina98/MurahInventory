package com.example.tokomurahinventory.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "merk_table",
    indices = [Index(value = ["refMerk"], unique = true)])
data class MerkTable (
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    @ColumnInfo(name="namaMerk")
    var namaMerk:String="",
    @ColumnInfo(name="refMerk")
    var refMerk:String=""
)