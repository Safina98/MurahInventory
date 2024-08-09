package com.example.tokomurahinventory.models

data class CountModel(
    var id:Int,
    var kodeBarang: String?,
    var merkBarang: String?,
    var isi: Double?,
    var psc:Int,
    var satuan:String?="",
    var logRef:String,
    var barangLogRef:String,
)