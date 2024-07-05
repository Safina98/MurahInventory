package com.example.tokomurahinventory.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date


@Entity(tableName = "detail_warna_table",
    indices = [Index(value = ["detailWarnaRef"], unique = true)],
    foreignKeys = [ForeignKey(entity = WarnaTable::class,
        parentColumns = ["warnaMerk"],
        childColumns = ["warnaMerk"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE)])
data class DetailWarnaTable(
    var id:Int=0,
    var warnaRef:String="",
    var meter:Double=0.0,
    var pcs:Int=0,
    var date:Date = Date(),
    var detailWarnaRef:String=""
)