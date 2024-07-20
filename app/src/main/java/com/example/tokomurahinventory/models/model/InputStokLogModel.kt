package com.example.tokomurahinventory.models.model

import androidx.room.ColumnInfo
import java.util.Date

data class InputStokLogModel (
    val id:Int,
    val namaMerk:String,
    val kodeWarna:String,
    val satuan:String,
    var pcs: Int = 0,
    var isi:Double=0.0,
    var barangLogInsertedDate: Date = Date(),
    // log_table
    var createdBy: String? = "",
    var inputBarangLogRef: String = ""
)