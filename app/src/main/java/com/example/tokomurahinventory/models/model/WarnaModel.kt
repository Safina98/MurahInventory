package com.example.tokomurahinventory.models.model

import androidx.room.ColumnInfo
import java.util.Date


data class WarnaModel(
    var idWarna: Int,
    var refMerk: String,
    var kodeWarna: String,
    var totalPcs: Int,
    var satuanTotal: Double,
    var satuan: String,
    var warnaRef: String,
    var totalDetailPcs: Int,
    var warnaCreatedDate: Date,
    var warnaLastEditedDate: Date,
    var createdBy: String?,
    var lastEditedBy: String?
)
