package com.example.tokomurahinventory.models.model

data class WarnaModel(
    var idWarna: Int,
    var refMerk: String,
    var kodeWarna: String,
    var totalPcs: Int,
    var satuanTotal: Double,
    var satuan: String,
    var warnaRef: String,
    var totalDetailPcs: Int
)
