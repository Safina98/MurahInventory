package com.example.tokomurahinventory.models.model

import java.util.Date

data class DetailWarnaModel(
    var detailWarnaIsi: Double,
    var warnaRef:String,
    var satuan:String,
    var detailWarnaPcs: Int,
    var createdBy:String?,
    var lastEditedBy:String?,
    var user:String?,
    var detailWarnaDate:Date,
    var detailWarnaLastEditedDate: Date,
    var detailWarnaKet:String?=""
)

