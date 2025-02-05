package com.example.tokomurahinventory.models.model

import java.util.Date

data class CombinedDataModel(
    val merkId: Int,
    val namaMerk: String,
    val refMerk: String,
    val merkCreatedDate: Date,
    val merkLastEditedDate: Date,
    val merkUser:String,
    val merkCreatedBy: String?="",
    val merkLastEditedBy: String?="",
    val warnaId: Int,
    val kodeWarna: String,
    val totalPcs: Int,
    val satuanTotal: Double,
    val satuan: String,
    val warnaRef: String,
    val warnaCreatedDate: Date,
    val warnaLastEditedDate: Date,
    val warnaUser:String,
    val warnaCreatedBy: String?="",
    val warnaLastEditedBy: String?="",
    val detailWarnaId: Int,
    val detailWarnaIsi: Double,
    val detailWarnaPcs: Int,
    val detailWarnaDate: Date,
    val detailWarnaLastEditedDate: Date,
    val detailWarnaUser:String,
    val detailWarnaCreatedBy: String?="",
    val detailWarnaLastEditedBy: String?="",
    val detailWarnaRef:String,
    val a:String?="",
    val dateOut:Date?=null,
    val dateIn:Date?=null
)
